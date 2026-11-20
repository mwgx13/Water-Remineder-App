const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, now = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    displayName: "Alex Rivera",
    dailyGoalMl: 2500,
    unit: "ML",
    quickPresetSmallMl: 150,
    quickPresetMediumMl: 250,
    quickPresetLargeMl: 500,
    wakeHour: 7,
    sleepHour: 22,
    createdAt: now,
    updatedAt: now,
  };
}

function validIntakeLogPayload(uid, { note = undefined, now = new Date(Date.now() - 5000) } = {}) {
  const payload = {
    userId: uid,
    amountMl: 250,
    effectiveMl: 250,
    beverageType: "WATER",
    dayKey: "2026-11-20",
    loggedAtMillis: Date.now() - 5000,
    createdAt: now,
    updatedAt: now,
  };
  if (note !== undefined && note !== null) {
    payload.note = note;
  }
  return payload;
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write profiles or intake logs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("intakeLogs").get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("intakeLogs")
      .doc("log_1")
      .set(validIntakeLogPayload(ALICE_UID))
  );
});

test("User profile: owner create, get, partial update succeed; invalid or shadow updates fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(profileRef.get());

  // Partial update of mutable fields succeeds
  await assertSucceeds(
    profileRef.update({
      dailyGoalMl: 3000,
      unit: "OZ",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject list on top-level /users collection
  await assertFails(aliceDb.collection("users").get());

  // Reject immutable createdAt modification
  await assertFails(
    profileRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field injection
  await assertFails(
    profileRef.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject out-of-bounds dailyGoalMl
  await assertFails(
    profileRef.update({
      dailyGoalMl: 100,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    profileRef.update({
      dailyGoalMl: 2800,
      updatedAt: new Date(Date.now() + 120000),
    })
  );
});

test("Intake logs: owner create (with/without note), list, update, delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logsCol = aliceDb.collection("users").doc(ALICE_UID).collection("intakeLogs");

  // Create without optional note
  await assertSucceeds(logsCol.doc("log_1").set(validIntakeLogPayload(ALICE_UID)));
  // Create with optional note
  await assertSucceeds(logsCol.doc("log_2").set(validIntakeLogPayload(ALICE_UID, { note: "Morning electrolytes" })));

  // List subcollection (mirrors Kotlin repository query)
  await assertSucceeds(logsCol.get());

  // Partial update
  await assertSucceeds(
    logsCol.doc("log_1").update({
      amountMl: 500,
      effectiveMl: 575,
      beverageType: "ELECTROLYTES",
      note: "Post workout",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Delete
  await assertSucceeds(logsCol.doc("log_2").delete());
});

test("Intake logs: reject @DocumentId 'id' field, invalid types/bounds, and immutable tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logsCol = aliceDb.collection("users").doc(ALICE_UID).collection("intakeLogs");

  // Reject payload containing 'id' field
  await assertFails(
    logsCol.doc("log_bad_id").set({
      id: "log_bad_id",
      ...validIntakeLogPayload(ALICE_UID),
    })
  );

  // Reject invalid beverageType
  await assertFails(
    logsCol.doc("log_bad_bev").set({
      ...validIntakeLogPayload(ALICE_UID),
      beverageType: "SODA",
    })
  );

  // Reject invalid dayKey
  await assertFails(
    logsCol.doc("log_bad_day").set({
      ...validIntakeLogPayload(ALICE_UID),
      dayKey: "2026/11/20",
    })
  );

  // Reject explicit null note (Kotlin filters null values out)
  await assertFails(
    logsCol.doc("log_null_note").set({
      ...validIntakeLogPayload(ALICE_UID),
      note: null,
    })
  );

  // Reject amountMl out of bounds
  await assertFails(
    logsCol.doc("log_huge").set({
      ...validIntakeLogPayload(ALICE_UID),
      amountMl: 9000,
    })
  );
});

test("Cross-user isolation: Alice cannot access or modify Bob's profile or logs", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("intakeLogs")
      .doc("bob_log_1")
      .set(validIntakeLogPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      dailyGoalMl: 1500,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("intakeLogs").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("intakeLogs").doc("bob_log_1").get());
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("intakeLogs")
      .doc("bob_log_2")
      .set(validIntakeLogPayload(ALICE_UID))
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("intakeLogs").doc("bob_log_1").delete());
});
