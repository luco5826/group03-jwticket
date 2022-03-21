const loadtest = require('loadtest');
const jwt = require("jsonwebtoken")
const fs = require("fs")
const outputFileName = "result.csv"
const privateKey = "ebFWkwyCkiYWbmZhoDvOOKSQnRayUzpOfQpfBWLWeshroGkQFULEkxwdRMvbjKYb"
const concurrentClients = [1,2,4,8,16,32]
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
    concurrency: 1,
    method: 'POST',
    body: { zone: "1", token  },
    contentType: 'application/json'

};
for (const clients of concurrentClients) {
    options.concurrency = clients
    loadtest.loadTest(options, function(error, result)
    {
        if (error)
        {
            return console.error('Got an error: %s', error);
        }
        console.log('Tests run successfully for %d', clients)
        fs.appendFileSync(outputFileName, `${clients},${result.totalRequests},${result.totalErrors},${result.totalTimeSeconds}, ${result.rps},${result.meanLatencyMs},${result.maxLatencyMs},${result.minLatencyMs}\n`)
        //console.log(result);
    });
}

