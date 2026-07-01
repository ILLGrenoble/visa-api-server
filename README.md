# VISA API Server and Remote Desktop Service

This project contains the source code for the API Server and the Remote Desktop service of the VISA platform.

VISA (Virtual Infrastructure for Scientific Analysis) makes it simple to create compute instances on facility cloud infrastructure to analyse your experimental data using just your web browser.

See the [User Manual](https://visa.readthedocs.io/en/latest/) for deployment instructions and end user documentation.

## VISA API Server

The VISA API Server is the main backend server of the VISA framework. It provides the following features:

- REST API for instance creation
- GraphQL API for dynamic admin interfaces and usage stats
- Instance management using (by default) OpenStack
- User management (including roles)
- Facility metadata (proposals, experiments, users and instruments)
- User notification via emails

### Design

At its most basic, the VISA API Server can be described as managing instance creation using an OpenStack client. However it performs a variety of functionality on top of this:

- User authorisation: VISA enables specific users to access specific instances. This can be due to their relationship to an instance (owner, member, etc) or their role within VISA (admin or support user for example)
- User management: VISA manages the different roles of users providing different levels of functionality depending on the user role.
- Instance lifecycle management: automatic deletion of instances after given times of inactivity or after a maximum lifetime
- Instance security/firewall rule management: VISA provides a basic set of rules to manage security groups for instance depending on the owner of an instance.
- User office metadata management: VISA provides a layer of data to include instruments, proposals, experiments and team members
- System usage stats: VISA can provide realtime analysis data for instances and sessions (real-time user usage of instances)

####  Micro-service enhancement
Customisation of VISA is possible through:

- a rich set of environment variables
- Custom micro-service developments (VISA Accounts and Security Group Service). These provide more control over the user authentication and also creation of security groups for individual instances

#### Cloud computing
VISA uses OpenStack as its cloud compute infrastructure. All management of instances uses the standard HTTP API of OpenStack. It is assumed that all partners are similarly adopting OpenStack for their compute infrastructure and VISA will only support this.

If other partners wish to deploy instances on a different infrastructure (eg Kubernetes) then a web-service to do this can be integrated into the VISA code. However this cannot be officially supported.

#### Database ETL
To account for many different types of User Office data sources (instruments, proposals, etc) across the partners, The VISA Common Portal works via a push mechanism to populate its databases. An external database ETL (Extract, Transform, Load) process is required to run at each site to inject data into visa.

Adapters at each site are required to extract and transform for their respective data sources and the VISA ETL will load the data into the VISA database.

## VISA Remote Desktop

The VISA Remote Desktop micro-service acts as a relay between Apache Gaucamole on a running instance and a web-socket to the browser client.

Both TCP-socket and web-socket connections are managed by this service. A client connects to an instance by providing an instance ID and a time-limited access token. It uses the Cloud Service to validate the access token and obtain the IP and Port of the services running on the instance.  It then establishes a connection (TCP Socket) to the instance and relays data from this connection to the web-socket (and vice-versa for user actions such as mouse and keyboard operations).

VISA Remote Desktop also manages user access to a desktop-enabled instance. All members of an instance can access a remote desktop through this service however only the owner is able to initiate a session.
Users can only access the remote desktop while the owner has an active session. All clients are disconnected when the owner closes their session.

Portal users who have specific support roles can also request access to remote desktops (provided the owner is connected).

Session data is also maintained by the service to provide usage statistics and real-time analysis of connected users.

## VISA Database management

The VISA database schema is created automatically when running the VISA API Server, it can however be created independently which can be useful when developing the ETL process before deploying VISA. VISA supports deploying the database to a PostgreSQL server, although in theory other databases are valid.

The schema is stored in a file within this project: `db/schema.sql` and can be used to populate the database as follows:

```bash
psql -h <server_host> -p <server_port> -U <db_user> -f db/schema.sql
```

### Database migrations

It is important to run the database migrations when deploying a new version of VISA. We use [dbmate](https://github.com/amacneil/dbmate) to manage the incremental database updates.

To update the database to the latest version you should run the following command:

```bash
dbmate -u "postgresql://<db_user>>:<db_password>@<db_host>:<db_port>/<db_name>?search_path=<db_schema>" --no-dump-schema up
```

This will apply all the necessary patches to the database.

## Acknowledgements

<img src="https://github.com/panosc-eu/panosc/raw/master/Work%20Packages/WP9%20Outreach%20and%20communication/PaNOSC%20logo/PaNOSClogo_web_RGB.jpg" width="200px"/> 

VISA has been developed as part of the Photon and Neutron Open Science Cloud (<a href="http://www.panosc.eu" target="_blank">PaNOSC</a>)

<img src="https://github.com/panosc-eu/panosc/raw/master/Work%20Packages/WP9%20Outreach%20and%20communication/images/logos/eu_flag_yellow_low.jpg"/>

PaNOSC has received funding from the European Union's Horizon 2020 research and innovation programme under grant agreement No 823852.
