const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Exams: can create valid exam", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("exams").doc("exam_123").set({
      id: "exam_123",
      title: "Operating Systems Midterm",
      accessCode: "VIA-101",
    })
  );
});

test("Exams: cannot create exam with empty id or title", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    db.collection("exams").doc("exam_invalid").set({
      id: "",
      title: "",
      accessCode: "VIA-101",
    })
  );
});

test("Submissions: can create valid submission", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("submissions").doc("sub_1").set({
      id: "sub_1",
      examId: "exam_123",
      studentName: "Yuvaraj",
      rollNo: "21CS101",
    })
  );
});

test("Submissions: cannot create invalid submission without rollNo", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    db.collection("submissions").doc("sub_2").set({
      id: "sub_2",
      examId: "exam_123",
      studentName: "Yuvaraj",
      rollNo: "",
    })
  );
});

test("Default deny: cannot write to arbitrary collection", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    db.collection("arbitrary").doc("doc1").set({
      data: "leak",
    })
  );
});
