# Installation

```
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

# Scripts

## create-instance

This script will create _n_ instances. 

> You will need a valid JWT token

Example usage:

This command will create _5_ instances and wait for _5_ seconds before creating another instance.

```bash
export TOKEN=MY_SUPER_LONG_ACCESS_TOKEN
./create-instances --count 5 --delay 5 --screen-width 1920 --screen-height 1080 --server https://visa.ill.fr --plan 27
```

## delete-account-instances

This script will an account's instances. 

> You will need a valid JWT token

Example usage:

This command will delete an instance every _5_ seconds.


```bash
export TOKEN=MY_SUPER_LONG_ACCESS_TOKEN
./delete-account-instances --delay 5 --server https://visa.ill.fr
```

This command will delete an instance with the state of _ERROR_ every _5_ seconds

```bash
export TOKEN=MY_SUPER_LONG_ACCESS_TOKEN
./delete-account-instances --delay 5 --server https://visa.ill.fr --state ERROR
```