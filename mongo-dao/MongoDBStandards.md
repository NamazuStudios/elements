# MongoDB Coding Standards

This outlines the MongoDB coding standards.

## Model Type Guidelines

Broadly, Elements divides model types into two separate categories. These are designated as user-owned and types used mostly for back office administration. The general guidance relates to the final size of the data set in production and whether or not the type can be sharded.

### General Guidelines

* ***ObjectId*** is used for primary key (ie ```_id```) property of a type.
  * A custom compound type may be used to mimic unique indexes to support sharding
  * When making a custom compound ID, use the ```Hexable``` type in order to ensure that the database ID can be used in RESTful APIs.
* Always use ```@Reference``` where possible. Do not store raw strings or object ids
* In general, put the ```@Index``` annotations at the top of the class not in fields.
* Index all fields that are used in a query.
* Null should not be valid for enum types.

### User-Owned Types

User owned types are created by users as normal operation of the application. Examples of types like this are Profiles, Inventory Items, Users, IAP receipts. It is reasonable to expect there are one or more per user.

* Avoid unique indexes as it would prevent sharding in production.

### Backoffice Admin Types

Backoffice Admin types are created by system administrators. Generally there are considerably fewer of these objects and the data set size is more predictable. Examples of this include Digital Goods, Applications, and Application Configuration.

* Use whatever indexing scheme makes the code easiest/fastest to write without needless complication.

## Testing

* A Guice module exists which is used to make tests. No new database code will be approved unless each method is backed by an integration test.
* Follow the examples found in ```mongo-dao/src/test/java``` for examples
* Tests should cover the following operations
  * Create, Read, Update Delete
  * Any strange edge cases.

## Dao Guidelines

* Do not use Request/Response types (eg ```CreateUserRequest```) in Dao code. Deal only with the exact model (eg ```User```).
  * Some code is written like this. If you find it. Refactor it if you can and it help clear up the code.
* 