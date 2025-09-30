#!/usr/bin/env node

const args = process.argv.slice(2);
const fs = require('fs-extra');

const name = args[0];
const dir = name && args[0].replace(/-/g, '_');

if (name) {
  console.log("Creating", name);
  fs.copySync(__dirname + "/template", name);
  fs.moveSync(name + "/gitignore", name + "/.gitignore");
  console.log("\nOk, you are ready to roll:");
  console.log("$ cd " + name);
  console.log("$ npm install");
  console.log("$ npm run watch");
  console.log("\nThen edit main.cljs\n");
} else {
  console.log("Usage: " + process.argv[1] + " APP-NAME");
}
