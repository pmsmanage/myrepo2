var http = require('http');
var dt = require('./mymodule.js');
var fetch = require('node-fetch');
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";


class ClientManager {
  constructor(keycloakServer, realmName, clientID, clientSecret){
    this.keycloakServer = keycloakServer;
    this.realmName = realmName;
    this.clientID = clientID;
    this.clientSecret = clientSecret;
    this.token = undefined;
    this.expire_time = 0;
  }

  async get_token() {
    if (new Date().getTime() > (this.expire_time +1000)){ //if there is only 1s left or less request new token
      await this.get_new_token()
    }
    return this.token
  }

  async get_new_token(){
    var form = new URLSearchParams();
    form.append('grant_type', 'client_credentials');
    form.append('client_id', this.clientID);
    form.append('client_secret', this.clientSecret);
    form.append('scope', 'openid');

    await fetch('https://'+ this.keycloakServer + '/auth/realms/'+ this.realmName + '/protocol/openid-connect/token', {
      method: 'post',
      body: form.toString(),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
    .then(response => response.json())
    .then(response => {
      console.log(response); //Temp
      this.token = response.id_token
      this.expire_time = new Date().getTime()+ response.expires_in*1000;
    })
    .catch(function (err) {
      console.log("unable to get new token", err);
      return undefined;
    });
  }

  async validateClientToken(token){
    var form = new URLSearchParams();
    form.append('token', token);
    var isvalid = "false";
    await fetch('https://'+ this.keycloakServer + '/auth/realms/'+ this.realmName + '/protocol/openid-connect/token/introspect', {
      method: 'post',
      body: form.toString(),
      headers: {'Content-Type': 'application/x-www-form-urlencoded',
                'Authorization': 'Basic '+btoa(this.clientID + ':' + this.clientSecret), 
    }
    })
    .then(response => response.json())
    .then(response => {
      console.log(response); //Temp
      if(response.typ == 'ID' && response.aud.includes(this.clientID) && response.client_id != this.clientID){
        isvalid = "true";
      }
    })
    .catch(function (err) {
      console.log("unable to introspect token", err);
    });
    return isvalid;
  }
}




var clientManager = new ClientManager('107.20.209.76:9443', 'dev-realm', 'node js', 'cfc12258-c729-429b-944a-eec657e2d583');

http.createServer(async function (req, res) {
  if(req.url=='/token'){
    var myresponse = await clientManager.get_token()
    res.writeHead(200, {'Content-Type': 'json'});
    res.write(JSON.stringify(myresponse));
    res.end();
  } else if (req.url=='/introspect'){
    var myresponse = await clientManager.validateClientToken('eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIxMEpobTVNeFpQRDA4Ry05VXY5aWZYY3hoanNaTnZ0eHhJdnNmMWFyZjRrIn0.eyJleHAiOjE2OTU3MTQ5MDMsImlhdCI6MTY5NTcxNDYwMywiYXV0aF90aW1lIjowLCJqdGkiOiI5MzRiN2M4Mi03NmI1LTQ5NzctODdkMi1mMTdhZTRiNzFkOWYiLCJpc3MiOiJodHRwczovLzEwNy4yMC4yMDkuNzY6OTQ0My9hdXRoL3JlYWxtcy9kZXYtcmVhbG0iLCJhdWQiOlsidG9mZnkiLCJub2RlIGpzIl0sInN1YiI6IjViMjY4MWIwLWYxYWItNGY0NS04M2U3LTdlOGZkOWY4M2IwYiIsInR5cCI6IklEIiwiYXpwIjoidG9mZnkiLCJzZXNzaW9uX3N0YXRlIjoiN2JlMzdkNjMtNGE4Ni00ZWQ3LThmM2ItZjZjMTZmY2EyYjIwIiwiYXRfaGFzaCI6IjJPRGQzU21BejdRNG5WMnVwejhKdGciLCJhY3IiOiIxIiwiY2xpZW50SWQiOiJ0b2ZmeSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY2xpZW50SG9zdCI6IjE4NS4xMTQuMTIxLjQ0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LXRvZmZ5IiwiY2xpZW50QWRkcmVzcyI6IjE4NS4xMTQuMTIxLjQ0In0.N76yf5z2VnKYw3ryyj30rxNHBzSWcstbieLjXNKZAKcWmFvJe8_JzPKES4SXXC_qvrKPMsoZJVTR2mAd2NTSdKeVcIa7Naf56waXUWAb836tSyZIFiWO7_clzfuLL77YVRxARi_Q2EAbm_Qg67DpauHyAkZMaV1Y_frrAFvs2zSXtRVbUmAqwo9Pc3nDrFT7T51LXV3RA04_oMYaK_oXrZ4_qgqC32C57O_dgVOvK6JBz60h9sjbJQnqN7WO2ax3WrqtCS6xQEVlE_4OlFPfiSOLlTH7SysLdKDdZoABolWyN_A-ueXVyyvitOZjTaIQ4-NeSejHFL9iiTtiVnffDg');
    res.writeHead(200, {'Content-Type': 'json'});
    res.write(myresponse);
    res.end();
  }
  console.log(req.url);
}).listen(8082);