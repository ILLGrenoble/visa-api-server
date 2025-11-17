3.6.0 17/11/2025
================
 * Flavour availability calculated
   * Determine from Hypervisor usage, Cloud limits and Device Pool Usage currently available flavours
   * Calculate future availability of flavours from current instance lifetimes and using the available sources of cloud usage
   * Flavour availability calculated with a confidence level depending on which sources of cloud usage are available
   * If flavour unavailable, calculate date when next available (show in UI when creating an instance)
 * OpenStack placement API added:
   * PCI Device usage automatically determined
   * Hypervisor resource availability and usage obtained
   * Obtain current server placement on hypervisors
   * Backward compatibility for unconfigured placement APIs (and web providers)
 * Customisable Flavour Lifetimes:
   * Role-based lifetimes can be added to flavours
   * Each flavour can have different lifetime rules
   * Flavour selection when creating instances show lifetime available
   * User can reduce lifetime if required
   * Config-based lifetimes are used if custom lifetimes not set (compatibility with previous versions)
   * Extensions requests take into account the lifetime rules
 * GPU (pci-passthrough) integration:
   * Add Device Pools data model
   * Devices determines from cloud flavour extra flags
   * Flavours automatically associated with configured device pools
   * Resource Classes from cloud (placement api in openstack) allow for automatic management of pci devices (GPUs)
   * Manual limits of devices possible
   * Admin interface for Flavours updated to allow for Device Pool configuration
   * Usage/limits of Devices obtained from either cloud or instance usage (depending on configuration)
   * Disable gpu flavour selection when gpus no longer available
   * Cloud limits on admin show device usage

3.5.2 16/10/2025
================
 * Fix null pointer exception when getting instance from instance authentication token
 * show image version in extension request email

3.5.1 11/09/2025
================
 * Handle server migration when shared storage is used: keep the instance active if the vm_state remains active during the migration

3.5.0 28/07/2025
================
 * Add personal invitation links to invite anyone to become a member of your instance: email the one-time-use link to someone to add them to the instance members. 
 * Public access token to share an instance with anyone who has the link. Can be disabled through config values. Access can be read-only/full access. Owner must be connected.
 * Use non-blocking emailing to avoid thread blocks if SMTP server is slow or blocking
 * Log to info the logging request filter to enable betting tracing of request and response times
 * Fix native build

3.4.3 16/07/2025
================
 * Fix file log format configuration

3.4.2 15/07/2025
================
 * Add basic auth for instance identity provider (allows an instance to obtain details of itself by passing a secret compute ID) - used as an alternative to cloud-init to get instance data.
 * Redesign of the AuthenticationMechanism: fix basic auth method for application credentials. 
 * update to quarkus 3.24.2 and update dependencies.

3.4.1 08/07/2025
================
 * Update WebX Relay to handle asynchronous creation of webx sessions (avoiding timeouts if the creation process is slow). Asynchronous requires WebX Router 1.5.0 but legacy routers are still supported.

3.4.0 03/07/2025
================
 * Small changes to error handling in redis pubsub.
 * Add connect and read timeout options for the cloud provider http clients.
 * Handle automatically accepting instance extension requests. Can be for no one, staff or everyone. Applies to individual images (configurable through admin ui).
 * Update WebX Relay to 1.7.0
 * Send remote desktop parameters to the cloud-init metadata (can be used to initialise a remote desktop session for the instance owner on startup).
 * Add "modal" type notifications (in parallel to legacy "banner" ones). Store user acknowledgements when a notification has been read.
 * Keep session Id of webx sessions active even when no one is connected. delete all sessions when an instance is deleted, rebooted or shutdown.
 * Test for secondary protocol being open when getting the instance state. Use the secondary protocol when creating the guacamole tunnel.
 * Add a secondary VDI protocol to the image: used for guacamole to specify the underlying remote desktop protocol (ie RDP).
 * Remove optional column from ImageProtocol. Check explicitly for the VDI protocol being available in the state instance action.
 * Send vdi protocol in instance state changed event (event gateway)
 * Modify instance update in account instance controller to handle vdi protocol.
 * Modify creation of instances with choice of vdi protocol from the client. Send default VDI protocol with Image to client. Send full ImageProtocol with Instance data to clients.
 * Modify creation and edition of images to handle default VDI protocol.
 * Automatically determine the instance session protocol if not explicitly set (from image or last instance session). Return protocol in DTOs.
 * Add "default VDI protocol" to Image and "VDI protocol" to instance. 
 * Send instance session VDI protocol to web client (for display in admin sections)

3.3.2 17/06/2025
================
 * Increase vertx warning exception time to 10s
 * Remove warning log when instance session member not found (can happen if the master api with the scheduler running is restarted but the session is on another server). 
 * update webx-relay to 1.6.0
 * Map guacamole long-name keyboard layouts to webx shortened ones.
 * Fix logging error on slow execution of VDI messages
 * Add a redis health monitor (optional... beta testing). Check pub-sub connection to redis every 10s. If subscriber fails then re-create the subscription.
 * Use virtual thread to publish redis messages: potential timeouts (from thread locks?).
 * Remove all explicit threading from the desktop message handling: run on the same websocket thread (blocking).
 * Add DesktopExecutorService to create a thread pool (system threads) for the creation/deletion of VDI sessions. Use a completable future to block to websocket request until the actions have completed. 

3.3.1 19/05/2025
================
 * Update webx-relay and send the webx-client version in the ClientConfiguration
 * more logging on remote desktop events (warning on slow event handling, info on connection times)
 * add logging on remote desktop connection event
 * Use Instant rather than Date for remote desktop event timing.

3.3.0 06/05/2025
================
 * Set a maximum inactivity time for each session: upon reaching this time the session is automatically disconnected.
 * Move instance session database updates to the DesktopSessionService.
 * Add jdbc pool size configuration options.
 * Handle disconnect by owner during access request from support user.
 * Start the connection thread after the DesktopSessionMember has been fully created: avoid client sending connection instructions prematurely.
 * Send Nop messages to WebXClient to keep the connection alive when support user is waiting for access to be granted to another desktop session.
 * Use mutiny timer rather than system thread to send NOP messages to clients that are waiting for desktop access.
 * Fix bug on InstanceSessionMember.active not being set to false when concurrent updates of ISMs occur (one to update the interactionAt and another to deactivate).
 * Add webx configuration parameters to the visa config. The parameters are sent to the WebX Router, and converted into environment variables when spawning a WebX Engine.
 * Update to relay 1.2.0 (waits for webx connection message).
 * Improve logging of event channel disconnection

3.2.4 22/04/2025
================
 * Use virtual threads rather than mutiny work pool threads. Use threadPerTask to ensure virtual thread use.

3.2.3 22/04/2025
================
 * Use un-synchronised "get" access to session members. Remove thread sleep bug.

3.2.2 17/04/2025
================
 * Ensure the event dispatcher subscriptions list access is synchronised
 * Limit use of synchronised to modify and access the lists of session and session members (fixed bug that blocked and dropped all sessions when connection to new session was slow).
 * More optimised retrieval of DesktopSessionMember directly from a hashmap (requires parallel management to list of desktop sessions).
 * Bug fix: corrected position in buffer array to obtain instruction type.
 * Redo locks and message queues: only lock/queue if waiting for completion of connection event.
 * Alternative to using mutiny workers: using virtual threads to run websocket events.

3.2.1 16/04/2025
================
 * Double check that the idle timer has been cancelled before calling the callback.
 * Increase idle timer to 60s (determine if this reduces the number of Idle timeouts for remote desktops).
 * Remove active jupyter sessions from view from instances that have been deleted.
 * Fix sql single result errors from possible double instance session members existing with the same client Id and session Id.

3.2.0 11/04/2025
================
 * Use the NopSender to regularly send messages to the guacamole client (ensures that sync messages are resent by the client keeping the connection alive)
 * Idle handler added to WebX client sessions
 * Full integration of WebX: web-relay v 1.1.0; connection via sessionId; thumbnail generation; screenshot; clipboard
 * Set the updatedAt to current timestamp when setting the instance session member data.
 * Improve access log (user agent details)

3.1.3 18/03/2025
================
 * Fix creating client auth tokens for invalid users

3.1.2 14/01/2025
================
 * Add color depth option to guacamole: default to 16 to maintain previous display quality, env var VISA_VDI_GUACD_PARAMETER_COLOR_DEPTH to set it to 8, 16, 24 or 32
 * Fix bug of emails being sent to owner rather than member added to instance
 * Fix instance filtering by instance state
 * Modify InstanceStateChangedEvent to send the termination date as the expiration date if the expiration date is null

3.1.1 17/12/2024
================
 * Minor bug fix to ensure that an instance expiration date is shown in the users' homepage. 

3.1.0 12/12/2024
================
 * Release version of VISA 3.X: tag alignment of visa-api-server, visa-web, visa-accounts and visa-jupyter-proxy

3.0.9 11/12/2024
================
 * Enabled graalvm native image build
 * Fix pagination being performed in memory for instances select (remove image protocols fetch)
 * FIX #21: read-only sessions were closed automatically by guacamole because sync events weren't being sent to the server
 * Handle case where 2 requests arrive at exactly the same time to create a remote desktop: keep only the latest instance session and remove any others
 * Verify that instances exist when creating tokens or thumbnails. Send 404 if not

3.0.8 10/12/2024
================
 * FIX #20: Ordering of admin instance sessions by most recent
 * FIX #19: Reduce the remote desktop idle timer to 14s preventing the guacamole timeout error from being reported
 * FIX #18: Use full entity inner classes for simplified models to ensure that foreign key constraints are created

3.0.7 08/12/2024
================
 * Increase instance server port check timeout default to 5s (from 2s)
 * Send ping messages from server to client to ensure that hidden tabs in chrome remain active (timers in js unreliable)
 * Delegate event channel socket functionality to GatewayTunnel
 * Use GatewayTunnel to encapsulate event channel subscription and gateway client. Improve async handling of socket events and timeout handler

3.0.6 06/12/2024
================
 * Fix lastSeenAt and lastInteractionAt being given the same value: ensure instance activity is only updated when instance has interaction
 * Add an idle handler to the GatewayClient to ensure that the events gateway is properly closed even if we don't receive a disconnection event from the websocket

3.0.5 05/12/2024
================
 * Improve logging from docker image: Specify log manager explicitly 
 * Handle all exceptions in openstack api calls
 * Handle cases where cloud provider has been removed but images, flavours and security groups are still configured
 * Allow for default cloud provider to be disabled (use only configured providers from the database)

3.0.4 04/12/2024
================
 * Automatically disconnect guacamole sessions if no ping event has been received in 30 seconds
 * Use smallrye timers to send error report emails rather than scheduler so that they can be sent even if scheduler is disabled
 * Properly queue desktop events and ensure they are run sequentially for a specific desktop session
 * Use of partial (or dto) objects and simplified models to reduce sql select and update overheads
 * Remove preql library
 * Many SQL optimisations to reduce loading time of several pages and also to minimise time spent in remote desktop event handler
 * FIX #15: Improve loading time of admin user page by removing calls to cloud provider for each user instance
 * FIX #14: Ensure client forces user to re-authenticate when token is no longer valid
 * FIX #13: Ensure that instance sessions members are deleted
 * FIX #12: Bug fix on instance interactivity being stored during many consecutive remote desktop events rather than just one 
 * FIX #11: Set socket state to disconnected immediately on disconnect event to avoid further data being sent to client
 * FIX #10: Avoid calls to cloud provider to ensure rapid response for admin instances page (ip address already in database)
 * FIX #9: SQL optimisations to obtain instance data, rapid load of home page with many instances
 
3.0.3 27/11/2024
================
 * FIX #6: Add responses for POST Requests in JupyterController
 * FIX #5: Avoid json serialization of instrument scientists  

3.0.2 25/11/2024
================
 * Fix bug of synchronising all activity updates on user interaction with remote desktop.  

3.0.1 21/11/2024
================
 * Determine if sockets are open before sending data
 * Add TLS options for Redis
 * Ensure that the instance activity recording is synchronised with any disconnection event to ensure we don't resave an instance session as active when it has already been closed
 * Modify logging of unauthorized access (warn rather than error) 

3.0.0 15/11/2024
================
 * Use a scheduler to handle error report emails (regroup multiple errors into a single email)
 * Send events to clients for instance list changes, instance state changes, general notifications, thumbnail updates
 * Send thumbnails using REST API rather than websocket
 * Add dedicated, authenticated websocket (events gateway) for all client events (in parallel to a WebX/SocketIO one)
 * Guacamole and WebX communication over a dedicated standard websocket rather than socket.io protocol 
 * Recoding of all load-balanced server communication (via Redis) (mono-server still possible without Redis)
 * Use explicit Quarkus Redis client and Quarkus websocket (running on same port as REST API)
 * Refactoring of remote desktop and use a standard websocket (remove socket.io implementation)
 * All dependencies updated
 * Build Docker image with Java 21
 * Remove joda-time, dozer and jackson-joda
 * Improve OpenStack API calls (including do not authenticate on every request)
 * Remove OkHttp and use Quarkus HttpClients for all REST API calls (OpenStack, Web providers, visa-accounts, visa-security-groups) 
 * Use Smallrye graphql (quarkus framework) and annotate all graphql queries, types and inputs
 * Separate visa-web into visa-web-rest and visa-web-graphql
 * Use Quarkus scheduler annotations
 * Use Quarkus mailer rather than SimpleJavaMail
 * Use JPA Annotations rather than XML files and improve lazy loading
 * Project migrated from Dropwizard to Quarkus

2.11.0 09/01/2024
================
* Integrate visa-print to enable printing to a local printer from the remote desktop

2.10.0 20/11/2023
================
 * Send instance UID to cloud-init
 * Add VISAFS protocol to fixtures

2.9.0 16/10/2023
================
 * Fix instance activity to register keyboard
 * Add daily job to remove instance activity data older than configured age (env var VISA_INSTANCE_ACTIVITY_RETENTION_PERIOD_DAYS)
 * Remove duplicates of cloud security groups
 * Allow security groups to be applied to specific flavours

2.8.1 10/05/2023
================
 * Fix anonymous access to /api/jupyter/instances/{instance}/notebook/close
 * Fix null pointer exception creating security groups.
 * Modify create/delete test scripts: assume that the token already includes the prefix "Bearer "

2.8.0 28/04/2023
================
 * Minor updates to standardise the API 

2.7.1 21/04/2023
================
 * Fix bug on role based security groups SQL

2.7.0 20/04/2023
================
 * Soft deletion of plans
 * User groups: allow groups of users to be created by admin. Groups work the same as roles and can be used in the management of flavours and security groups.

2.6.0 21/03/2023
================
 * Ensure instance expirations are removed when an instance termination date is set to null (immortal)
 * Add role-based flavour limits (flavour only available to users with specified roles)
 * Add WebX as a remote desktop implementation (some features unavailable such as instance sharing and screen capture)
 * Refactor remote desktop to add abstraction layer to implementation

2.5.1 23/01/2023
================
 * Fix tests
 * Fix bug on experiment search failing when ordering by instrument or proposal identifier.

2.5.0 20/12/2022
================
 * Check whether visa is in open-data mode before sending back all the members of the proposals: empty the list of the user is not part of the original proposal.

2.4.4 22/11/2022
================
 * Fix bug on creation of instances with open data (experiment check takes into account open data)

2.4.3 21/11/2022
================
 * Add env var (VISA_INSTANCE_USER_DEFAULT_QUOTA) to allow configuration of default instance quota for users created by VISA

2.4.2 18/11/2022
================
 * Add possibility to search for open data (disabled by default, enabled with env var VISA_CLIENT_CONFIG_INCLUDE_OPEN_DATA)
 * Allow protocols to be optional (allow remote desktop to be available before Jupyter starts)
 * Change default SSO scope (remove offline_access)

2.4.1 15/11/2022
================
 * Add url and doi columns to experiment and proposal tables
 * Filter by DOIs or Proposal Identifiers when getting experiments for a user 

2.4.0 24/10/2022
================
 * Record mouse and keyboard activity on instances to maintain history of when users have interacted with instances
 * Allow users to indicate whether others can access their instance when they are not connected
 * Allow for multiple cloud providers: Cloud providers can be configured from database settings as well as the default config file
 
2.3.2 23/09/2022
================
 * CC the admins in the email sent when replying to instance extension requests (keep all team informed)
 * Allow experiments to have specific titles (different to proposal titles): use proposal title by default
 
2.3.1 08/09/2022
================
 * Use a separate email address for the instance creation emails (env var VISA_NOTIFICATION_EMAIL_ADAPTER_DEV_EMAIL_ADDRESS) 
 
2.3.0 01/09/2022
================
 * Filtering of experiments that don't have valid start or end dates
 * Add admin notifications (currently for instances in error and instance extension requests) shown in admin UI
 * Add endpoints to handle instance lifetime extension requests and email templates for the extension request workflow
 * Add GUEST role and expiration date to role (provides time limited access to VISA to users that aren't associated to proposals)
 * Add health check endpoint for use by application credentials
 * Authentication via application credentials - can access specific API endpoints using basic auth. Endpoint to manage app credentials.

2.2.1 09/05/2022
================
 * Fix bug on notification service throwing an error when an adapter is not enabled

2.2.0 23/03/2022
================
 * Update the instance creation and deletion scripts (used for testing cloud providers)
 * Add database migration scripts for use with dbmate (include in Docker container too)
 * Fix some OpenStack issues by accepting case-insensitive headers
 * Modify system notifications to allow them to be re-used (activate/deactivate) and soft delete
 * Add UID to Instance model to be used in client routes and server REST API
 * Update library dependencies
 * Fix email template typo
 * Ensure HTTP client requests and responses are closed correctly

2.1.1 26/11/2021
================
 *  Catch 401 errors from the account service and log accordingly (don't log as an error).

2.1.0 17/11/2021
================
 * Update configuration for openid connect
 * Fix issue with log level set to debug
 * Filter experiments by start date only
 * Create users automatically if they do not exist in the database 
 * Allow for empty analytics configuration
 * Update persistence XML files to conform to JPA schema 2.2

2.0.2 30/09/2021
================
 * Add Web Cloud Provider (cloud instances managed through a middleware)
 * Add graphQL endpoints for security group, security group filters and flavour limit management
 * Remove Cycle table and all references
 * Add errors to payload of paginated responses
 * Allow for searching of experiments by proposal Id
 * Add user role management admin UI

2.0.1 30/07/2021
================
 * Set activated and update lastSeenAt when users access visa
 * Fix bug on experiment count criteria builder
 * Add graphql endpoint to update instance termination date
 * Add graphql endpoints for jupyter session stats
 * Upgrade to guice 4.2.3: better error handling on startup
 * Send VISA PAM public key as metadata to instance
 * Fix email template env vars
 * Fix instance expiration bug: check for deleted instances correctly
 * Fix environment variable names

2.0.0 15/06/2021
==============
 * VISA platform open sourced and moved to GitHub
 * Updated docker build
 * Add autologin to images (optional use of visa pam module)  
 * Enable/disable admin emails
 * Explicit naming of foreign keys, unique keys and constraints
 * Searching for experiments using dates rather than cycles
 * Email template directory configuration
 * Add instance attributes table and pass to openstack 
 * Migrate Instrument Responsible to Instrument Scientist
 * Use VISA Accounts micro-service for authentication
 * Convert many database booleans to timestamps (eg instance deleted_at)
 * Add summary to proposal
 * Remove dependency on Cycle in business logic (eg instrument control support user)  
 * Add start and end date to ExperimentF
 * Use Security Group Service to determine instance security groups (logic removed from API Server)
 * Make user IDs strings rather than longs

---

1.0.23 30/04/2021
=================
 * Add keyboard_layout to instances
 * Fix bug on integer overflow for instance session durations
 * Add deleted_at date to instances
 * Hide plans that have deleted flavours

1.0.22 22/01/2021
=================
 * Added Configuration table/entity (and repository + service) for dynamic server configuration
 * Filter security groups by role for staff when creating instances
 * Increase inactivity duration for staff to 8 days (keep 4 days for non-staff)

1.0.21 13/01/2021
=================
 * Modified account instances endpoint to enable filtering of instances by experiments or roles
 * Added allowedRemoteClipboardUrlHosts to configuration to allow client to suggest opening URLs in another tab when copied to clipboard in instance
 * Soft delete flavours
 * Send list of experiments and instruments to instances (openstack parameters) for future module env
 * Fix bug on jupyter session database entries

1.0.20 30/11/2020
=================
 * Upgraded guacamole library
 * Specifying guacd options inside configuration (i.e. ignore-certs and rdp port)
 * Added username and home directory to graphql types

1.0.19 19/11/2020
=================
 * Fix hibernate problems: always obtain the latest instance object from the DB rather than using the session version.

1.0.18 13/11/2020
=================
 * Filter plans by user and experiments: allow IRs on selected instruments to access to large flavour automatically 
 * Change instance lifetime to 60 days for staff
 * Allow only owners to access Jupyter notebooks
 * Sort proposed plans by flavour
 * Added a preset to the plan to provide default image+flavour to users

1.0.17 26/10/2020
=================
 * Modify openstack instance cleanup code to remove all zombie instances
 * Add scientific computing role (available as a scientific support user on instance members UI)
 * Add instance jupyter session data for statistical analysis of jupyter usage in visa
 * Add endpoints to be used by visa-jupyter-proxy (keepalive and session data)
 * Use image protocol service to associate ports to an image (remove hardcoding and allow for jupyter notebook to be activated dynamically)
 * Store the instance IP address in the instance table
 * Send instance ID and user home directory as metadata to openstack when creating the instance

1.0.16 03/09/2020
================
 * Add Employer model as affiliation to User rather than simple string name for better stats
 * Get all support users rather than staff for instance support members
 * Change instance handling in actions to avoid transaction problems
 * Increase max frame payload length to 200KB to avoid exceptions on large thumbnails 
 * Catch exceptions when sending emails for expiration notifications 
 * Allow instrument responsible and admin users to connect using the support page to external user instances when the owner is disconnected.

1.0.15 25/08/2020
================
 * Storing thumbnails as base64 instead of blobs
 * Added system notification graphql endpoints
 * Reduce race conditions on instance states by executing commands on a single load balanced server

1.0.14 18/08/2020
================
 * Add cloud instance cleanup job
 * Ordering of cycles by most recent
 * Modify logging of connected user and users
 * Added thumbnail upload over websocket
 * Remove name and description from Plan
 * Log and create error if userId equal to 0
 * JProfiler checks: make repositories and services singletons

1.0.13 15/08/2020
================
 * Use socket.io ping configuration
 * Add graphql endpoint to get interactive sessions
 * Add more instance information to remote desktop logs

1.0.12 14/08/2020
================
 * Handle deletion of instances while they are stopping
 * Fix date format for Safari
 * Added analytics information to client configuration
 * Added lastInteractionAt to InstanceSessionMember

1.0.11 13/08/2020
================
 * Fix guacd timed out problem using socketio pings to detect client socket closure.

1.0.10 11/08/2020
================
 * Add graphql endpoint for recently active users
 * Update delete instance mutation in graphql
 * Send instance lastInteractionAt to clients
 * Fix gitlab CI

1.0.9 10/08/2020
================
 * Fix date format problem for graphql types
 * Add lastInteractionAt to instance to determine when last mouse or keyboard event was made
 * add endpoint to get user by last name 
 * Change dashboard to show number of images and flavours in use by the instances
 * Reduce log to warn for unauthorized access to a remote desktop
 * Add active sessions to instance resolver
 * Extra logging on command execution

1.0.8 09/08/2020
================
 * Fix mapping of users of experimental team
 * Get sessions for individual instances
 * Fix syslog config
 * Change sql request for instrument control support users' instances (get all instances with experiments in a 7 day window)
 * Improved error logging and emails
 * Add hikari database connection pool

1.0.7 06/08/2020
================
 * Increase request timeout to OpenStack API
 * Close access request modal on owner's machine when user cancels request (multi server env ok)
 * Bug fix: Handle multiple current sessions for an instance
 * Add instance quota to users
 * Scripts for creating and deleting instances (for OpenStack perf testing)
 * Extra information in error logs
 * Graceful deletion of instances: shutdown first
 * Add boot command to Image and sent it to an instance when it is created
 * Send metadata (owner username) to OpenStack on instance creation

1.0.6 04/08/2020
================
 * Added gitlab CI for docker image creation
 * Add file appender and syslog appender
 * Get only active plans for admin interface
 * Soft delete sessions and session members when deleting an instance 
 * Add 1 week before and after cycle for security group management to allow access to instruments during this period 
 * Add random name generator
 * Send emails to members when they are added to an instance
 * Add email appender to send emails when an error log message occurs 
 * Improve logging details (including user and instance IDs)
 * Added last seen at to users for statistics 

1.0.5 23/07/2020
================
 * Allow the owner to disconnect specific clients from an active desktop session
 * Endpoint to return active sessions for an instance
 * Add version number to image
 * Remove stale sessions on server startup
 * Option to change access to read-only when the owner disconnects (room locked) rather than disconnecting all other clients
 * Handle read-only access to desktops using the 'display' event listener (drop keyboard and mouse events)
 * Notify clients when access has been granted by owner to share their desktop
 * Fix bug on instance expirations not being deleted if instance is deleted manually first

1.0.4 20/07/2020
================
 * Desktop access requests for dynamic access to remote desktops for support users (messaging to owner for explicit access grants when support user wants to connect), multi-server model ok
 * Obtain list of instances for the different support roles with filtering
 * Add different support roles (instrument control, IT support  and instrument responsible)

1.0.3 13/07/2020
================
 * Bug fix: change url to get support users

1.0.2 10/07/2020
================
 * Get instances for instrument control support
 * Mapping instrument responsibles
 * Added system notifications

1.0.1 09/07/2020
================
 * Change ScientificSupport role to Staff role and remove automatic access to instances (used for security groups)
 * GraphQL session endpoints

1.0.0 07/07/2020
================
 * Server client configuration data to determine server version and keycloak information (avoids multiple environments for client, facilitates docker config)
 * Add protocols to image update in graphql
 * Docker configuration
 * Bug fix: remove cascade of updating images when saving instances (remove also instance object from remote desktop connection session data)
 * Add activated flag to users to determine which ones have connected at least once to visa
 * Add plans to graphql
 * Add owner username to instance to allow for connections when owner is absent
 * OpenStack security groups managed purely through database entries (remove from config)
 * Store session connection history in database
 * Load-balancing of desktop connections using a redis server

#9062490 18/06/2020
===================
