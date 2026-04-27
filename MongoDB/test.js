import mongoose from "mongoose";
import User from "./models/user.model.js";

async function run() {
  await mongoose.connect("mongodb://127.0.0.1:27017/test-db");

  console.log("Connected to DB");

  const user = new User({
    username: "testuser3",
    email: "test3@example.com",
    password: "123456"
  });

  await user.save();

  console.log("User saved:", user);

  process.exit();
}

run();