require("dotenv").config()
const loadtest = require('loadtest');
const jwt = require("jsonwebtoken")
const fs = require("fs")
const yargs = require("yargs")
const {hideBin} = require('yargs/helpers')
const argv = yargs(hideBin(process.argv))
    .option('clients', {
        alias: 'c',
        demandOption: true,
        describe: "Number of concurrent clients",
        type: "number"
    })
    .usage("node index.js --clients [number]")
    .argv
const outputFileName = "result.csv"
const privateKey = process.env["jwt.key"]
const url = 'http://localhost:8080/validate'
const token = jwt.sign({
        "sub": "user",
        "name": "User",
        "vz": "123",
        "exp": 1650361338, // 19-04-2022 09:42:18
        "iat": 1516239022
}, privateKey, { algorithm: 'HS256'});

let options = {
    url,
    maxRequests: 10000,
    concurrency: argv.clients,
    method: 'POST',
    body: { zone: "1", token  },
    contentType: 'application/json'

};

loadtest.loadTest(options, (err, result) => {
    fs.appendFileSync(outputFileName, `${argv.clients},${result.totalRequests},${result.totalErrors},${result.totalTimeSeconds}, ${result.rps},${result.meanLatencyMs},${result.maxLatencyMs},${result.minLatencyMs}\n`)
    console.log(`Test finished for ${argv.clients} clients`)
})
