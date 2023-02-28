# Board

Board project represents our current best shot at modelling the Polish social structure,
as required for pdyn2 and the current release of pdyn1.5.

The model represents people, households, workplaces etc., but does not contain
anything covid-specific (or even epidemic-specific).

The idea is to create a universal social structure, as well (i.e. "close to reality")
as we can, and then convert it for specific needs of any application, by - for example
- creating heterogenic epidemic contexts (`Context` components) from Educational Institutions, Workplaces
  and Households.
 
The structure can also be:

- exported to a CSV file
- exported to a SQL database and inspected using standard tools
- exported to _.dat_ files compatible with the original PDYN software
- exported (in different projections) to VisNow (a _.vnf_ file)

## Versions of the data model

The data model evolves with needs of dependent projects and the availability
of resources. The git repository of the engineIo project represents only the latest version,
but since structures generated with the older versions can exist and still be used somewhere,
all the release versions are described in separate, parallel documents:

- 2021-09-10 - [pdyn 1.5 v1.1](pdyn15_v1_1/model_pdyn15_v1_1.md) (the current version)
- 2021-08-02 - [pdyn 1.5 v1.0](pdyn15_v1_0)


