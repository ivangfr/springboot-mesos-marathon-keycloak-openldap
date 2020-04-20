# springboot-mesos-marathon-keycloak-openldap

## Configure Keycloak Manually

![keycloak](images/keycloak.png)

### Login

- To open `Keycloak` there are two ways

  1. In a terminal, run the commands below and access the link printed from the echo command
     ```
     KEYCLOAK_ADDR="$(curl -s http://localhost:8090/v2/apps/keycloak | jq -r '.app.tasks[0].host'):$(curl -s http://localhost:8090/v2/apps/keycloak | jq '.app.tasks[0].ports[0]')"

     echo "http://$KEYCLOAK_ADDR"
     ```
  1. Using [`Marathon`](http://localhost:8090)

- The `Keycloak` website credentials
  ```
  Username: admin
  Password: admin
  ```

### Create a new Realm

- Go to top-left corner and hover the mouse over `Master` realm. A blue button `Add realm` will appear. Click on it.
- On `Name` field, write `company-services`. Click on `Create`.

### Create a new Client

- Click on `Clients` menu on the left.
- Click `Create` button.
- On `Client ID` field type `simple-service`.
- Click on `Save`.
- On `Settings` tab, set the `Access Type` to `confidential`.
- Still on `Settings` tab, set the `Valid Redirect URIs` to `http://localhost:8080`.
- Click on `Save`.
- Go to `Credentials` tab. Copy the value on `Secret` field. It will be used on the next steps.
- Go to `Roles` tab.
- Click `Add Role` button.
- On `Role Name` type `USER`.
- Click on `Save`.

### LDAP Integration

- Click on the `User Federation` menu on the left.
- Select `ldap`.
- On `Vendor` field select `Other`
- On `Connection URL` type `ldap://ldap-host`.
- Click on `Test connection` button, to check if the connection is OK.
- On `Users DN` type `ou=users,dc=mycompany,dc=com`
- On `Bind DN` type `cn=admin,dc=mycompany,dc=com`
- On `Bind Credential` set `admin`
- Click on `Test authentication` button, to check if the authentication is OK.
- On `Custom User LDAP Filter` set `(gidnumber=500)` to just get developers.
- Click on `Save`.
- Click on `Synchronize all users`.

### Configure users imported

- Click on `Users` menu on the left.
- Click on `View all users`. 3 users will be shown.
- Edit user `bgates`.
- Go to `Role Mappings` tab.
- Select `simple-service` on the combo-box `Client Roles`.
- Add the role `USER` to `bgates`.
- Do the same for the user `sjobs`.
- Let's leave `mcuban` without `USER` role.