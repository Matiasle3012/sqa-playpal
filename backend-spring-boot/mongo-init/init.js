// Seed data for PlayPal (database: TEST)
// Runs automatically on first container start (docker-entrypoint-initdb.d).
// Mirrors the domain model in src/main/java/com/api/playpal/**/domain:
// User -> Branch -> Court -> Reservation (all linked via @DBRef).

db = db.getSiblingDB("TEST");

// ---- Fixed ids so relationships between collections are predictable ----
const providerId = ObjectId("111111111111111111111111");
const clientId = ObjectId("111111111111111111111112");

const branch1Id = ObjectId("222222222222222222222221");
const branch2Id = ObjectId("222222222222222222222222");

const court1Id = ObjectId("333333333333333333333331");
const court2Id = ObjectId("333333333333333333333332");
const court3Id = ObjectId("333333333333333333333333");

const reservation1Id = ObjectId("444444444444444444444441");
const reservation2Id = ObjectId("444444444444444444444442");

// BCrypt hash for plaintext password "password123" (Spring Security BCryptPasswordEncoder compatible)
const testPasswordHash = "$2b$10$N7ZT4M9RUpk/5NX2c6o2yeM4XFhevrquA4A.q1WJGuVYdq2boCjOW";

function dbRef(collection, id) {
  return { "$ref": collection, "$id": id, "$db": "TEST" };
}

// ---- users ----
db.users.insertMany([
  {
    _id: providerId,
    username: "carlos_provider",
    email: "carlos.provider@playpal.test",
    password: testPasswordHash,
    created_at: "2026-07-01T09:00:00",
    role: "provider",
    branches: [dbRef("branches", branch1Id), dbRef("branches", branch2Id)],
    thumbnail_url: null
  },
  {
    _id: clientId,
    username: "maria_user",
    email: "maria.user@playpal.test",
    password: testPasswordHash,
    created_at: "2026-07-02T10:30:00",
    role: "user",
    branches: [],
    thumbnail_url: null
  }
]);

db.users.createIndex({ email: 1 }, { unique: true });

// ---- branches ----
db.branches.insertMany([
  {
    _id: branch1Id,
    name: "Complejo Deportivo Central",
    city: "Santiago",
    street: "Av. Providencia 1234",
    courts: [dbRef("courts", court1Id), dbRef("courts", court2Id)],
    thumbnail_url: null,
    provider: dbRef("users", providerId)
  },
  {
    _id: branch2Id,
    name: "PlayPal Las Condes",
    city: "Santiago",
    street: "Av. Apoquindo 5678",
    courts: [dbRef("courts", court3Id)],
    thumbnail_url: null,
    provider: dbRef("users", providerId)
  }
]);

// ---- courts ----
db.courts.insertMany([
  {
    _id: court1Id,
    sports: ["futbol"],
    type: "Cancha de futbol 5",
    number: 1,
    price: NumberLong(25000),
    description: "Cancha techada de pasto sintetico",
    hours: "09:00-23:00",
    thumbnail_url: null,
    branch: dbRef("branches", branch1Id),
    branchName: "Complejo Deportivo Central",
    branchCity: "Santiago"
  },
  {
    _id: court2Id,
    sports: ["padel"],
    type: "Cancha de padel",
    number: 2,
    price: NumberLong(18000),
    description: "Cancha de padel panoramica",
    hours: "08:00-22:00",
    thumbnail_url: null,
    branch: dbRef("branches", branch1Id),
    branchName: "Complejo Deportivo Central",
    branchCity: "Santiago"
  },
  {
    _id: court3Id,
    sports: ["tenis"],
    type: "Cancha de tenis",
    number: 1,
    price: NumberLong(20000),
    description: "Cancha de polvo de ladrillo",
    hours: "07:00-21:00",
    thumbnail_url: null,
    branch: dbRef("branches", branch2Id),
    branchName: "PlayPal Las Condes",
    branchCity: "Santiago"
  }
]);

// ---- reservations ----
db.reservations.insertMany([
  {
    _id: reservation1Id,
    court: dbRef("courts", court1Id),
    user: dbRef("users", clientId),
    start: "10:00",
    end: "11:00",
    date: ISODate("2026-07-20"),
    active: true,
    createdAt: "2026-07-14T09:00:00",
    totalPrice: NumberLong(25000)
  },
  {
    _id: reservation2Id,
    court: dbRef("courts", court3Id),
    user: dbRef("users", clientId),
    start: "18:00",
    end: "19:00",
    date: ISODate("2026-07-21"),
    active: true,
    createdAt: "2026-07-14T09:05:00",
    totalPrice: NumberLong(20000)
  }
]);

print("PlayPal TEST database seeded: " +
  db.users.countDocuments() + " users, " +
  db.branches.countDocuments() + " branches, " +
  db.courts.countDocuments() + " courts, " +
  db.reservations.countDocuments() + " reservations.");
