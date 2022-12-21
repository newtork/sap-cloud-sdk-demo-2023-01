# Demo application

... to showcase SAP Cloud SDK in action.

## Use case

Spring Boot application to call an SAP S/4HANA OData endpoint.

Concrete benefits of Cloud SDK:
* Hide complexity of establishing connectvity and SAP BTP service communication:
  * _Authentication and Authorization Service_ (XSUAA)
  * _Identity Service_
  * _Destination Service_
  * _Connectivity Service_



## Branches

* [x] `main/vanilla`
  * No usage of SAP Cloud SDK, implementation is done from scratch.
* [x] `main/v3` ([diff](https://github.com/newtork/sap-cloud-sdk-demo-2023-01/compare/main/vanilla...main/v3))
  * Best practices applied from most recent v3 release.
* [x] `main/v4` ([diff](https://github.com/newtork/sap-cloud-sdk-demo-2023-01/compare/main/v3...main/v4))
  * Migration to version v4


## (Open)

* Create vanilla sample code branch.
* Motivate asynchronous task to propagate `ThreatContext`.
* Motivate @Async method annotation.
* Motivate @Header argument annotation.
