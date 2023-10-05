# Keycloak
integrating keycloak with spring boot and node js

## keycloak config
creating realms:

![image](https://github.com/pmsmanage/myrepo2/assets/135229465/679da642-0707-4338-842d-21daab7bb8b5)

adding clients:
![image](https://github.com/pmsmanage/myrepo2/assets/135229465/3ac9e727-1cfc-40cc-a436-8ebb04f8d04e)

allow confidentials:

![image](https://github.com/pmsmanage/myrepo2/assets/135229465/dd0186ab-cb05-47aa-931b-3cc329efc621)

turn on service account enabled

put any url in "Valid Redirect URIs" this is used when client login through console api (won't be used)

and then hit save (at the bottom)


creating audience mapper: after creating clients you should add audience wich will provide info about what clients the tokens generated made for, this will prevent token forwarding, a client that recieve a token without his id in aud should reject it

go to client and select mappers from top bar and then create

![image](https://github.com/pmsmanage/myrepo2/assets/135229465/968fa8b3-2436-4894-9089-14bb52b20a37)

add audient client to "Included Client Audience" and turn on "add to token id"

this proccess should be done to every new audience need to be add, new id token of this client will include the new audience client in it


 client secret located under credentials, client id and secret will be used to request tokens

 ## spring boot and node js

 provide related information to class "client manager" and it should be ready to use keycloak endpoints to perform security

 in your spring/node controllers any endpoint that should be only acceseed from a client, requests to it must should have "client_token" which have id token of the client made the request
 
 reciever should use this token to verify the identity of the client made the request, reciever should include it's id_token in the response so sender can verify that response came from that client
