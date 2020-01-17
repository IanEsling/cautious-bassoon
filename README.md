## Intro

1. This is a service for transferring funds between 2 accounts
1. This is a runnable service that exposes 2 REST API endpoints:
    1. `/transfer` takes a PUT request with a JSON body comprised of 3 required fields, `fromAccount`, `toAccount` and `amount`.
        Assuming no errors, the `amount` will be transferred from `fromAccount` to `toAccount` and an empty 201 response returned
    1. `/balance/{accountRef}` takes a GET request and returns a 200 response with a JSON body consisting of `accountRef` and `balance`
1. This service can be run
    1. from an IDE by executing the `Main` class
    1. as an executable jar from the command line. From the project root run `mvn clean package` then when that has completed you can run the packaged JAR file with `java -jar target/revolut-1.0-SNAPSHOT.jar`
1. when running the service can be found on `localhost:9000`
   
## Data
1. When the service starts up it loads a fixed set of data into memory:
    1. `accounts` - there are 4 accounts with references "Ref1", "Ref2", "Ref3" and "Ref4"
    1. `transfers` - a series of historical transfers that have already taken place between accounts
    1. `startingBalance` - a fixed amount that each account is deemed to start with (each account starts with the same amount.) 
    1. The balance of each account is calculated when the service starts up by applying the historical transfers to the starting balance
1. subsequent successful transfers are added to the `transfers` collection and the balance for each account recalculated
1. nothing is serialized including when the service stops so all transfers made since start up will be lost and the service will just reload the same historical transfers at next start up 

## Checks and Errors
1. all 3 fields are required for a successful transfer, if any are missing a 400 response is returned.
1. the amount has a scale of 2, an attempt to transfer an amount with more than 2 decimal places will return a 400 response 
1. attempting to transfer from or to an unknown account (i.e. an account that isn't one of the 4 known accounts loaded at start up) will throw a 400 response
1. no account is allowed a negative balance so attempting a transfer that will leave the `fromAccount` with less than zero in it will return a 400 response
1. attempting to get the balance of an unknown account will return a 404 response
1. all transfers must be for a positive amount, specifying an amount of zero or less will return a 400 response
1. attempting to transfer to and from the same account will return a 400
