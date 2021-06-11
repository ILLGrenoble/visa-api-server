2.0.0-SNAPSHOT
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
 * Add start and end date to Experiment
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
