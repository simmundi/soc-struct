# Dictionary of the terminology used in prefabTag production

<dl>
    <dt>Replicants</dt>
        <dd>Agents in pdyn 1.5 created with assigned personal properties (according to respective heuristics) at the time of creating their households; Replicants are agents outside of the basic GUS model. 
        The replicants have at least one of two important aspect:
            (1) special properties which require additional, very specific parameters (like a maximum distance between a dorm house and a university); or (2) their properties are strongly correlated in a custom way (e.g. all members of dorm households are students of the same university; all prisoners in the same cell have the same gender, don't work and don't attend any educational institution).   
        To put it in another way: replicants are extreme exceptions to the statistics which rule the core of our society. They influence the structure in a way which would never emerge on its own. Those properties preclude replicants from being created in our usual, organic process. We need another pass with separate procedures.</dd>
    <dt>ReplicantType</dt>
        <dd>Type of replicants.</dd>
    <dt>ReplicantsCounter</dt>
        <dd>Counter of all replicants.</dd>
    <dt>ReplicantsPopulation</dt>
        <dd>Agents pool from which replicants are created. To help keep the population statistics correct, all replicants are created from one common pool  of "people" - which itself is based on Wrocław's population from our GUS data (for no special reason).</dd>
</dl>

## Replicant contexts' units types

<dl>
    <dt>Barracks</dt>
        <dd>Military barracks. `ReplicantType.BARRACKS`</dd>
    <dt>ClergyHouse</dt>
        <dd>Clergy houses. `ReplicantType.CLERGY_HOUSE`</dd>
    <dt>ImmigrantsSpot</dt>
        <dd>Households where immigrants live. `ReplicantType.IMMIGRANT_SPOT`</dd>
    <dt>NursingHome</dt>
        <dd>Nursing homes. `ReplicantType.NURSING_HOME`</dd>
    <dt>Prison</dt>
        <dd>Prisons. `ReplicantType.PRISON`</dd>
    <dt>Dorm</dt>
        <dd>Students' residence houses. `ReplicantType.DORM`</dd>
    <dt>Monastery</dt>
        <dd>Monasteries. `ReplicantType.MONASTERY`</dd>
    <dt>HomelessSpot</dt>
        <dd>Spots where homeless people live. `ReplicantType.HOMELESS_SPOT`</dd>
</dl>

## Replicant creation

<dl>
    <dt>TypeUrizen</dt>
        <dd>The class that creates replicants of a given type.</dd>
    <dt>typeRoom</dt>
        <dd>Single context stacked into the unit of a given type.</dd>
    <dt>typeResident</dt>
        <dd>Replicant agent living in the context of a given type.</dd>
</dl>

## Replicant properties 
<dl>
    <dt>typeReplicantsCount</dt>
        <dd>Number of all replicants of given type.</dd>
    <dt>typeRoomSize</dt>
        <dd>Number of agents in the room.</dd>
    <dt>typeMaxRooms</dt>
        <dd>Maximum number of rooms in the unit.</dd>
    <dt>dormToUniversityMaxDistance</dt>
        <dd>Maximum distance from dorm to the university.</dd>
    <dt>homelessSpotMaxInSingleGridCell</dt>
        <dd>Maximum number of homeless spots in one grid cell.</dd>
    <dt>homelessSpotPercentOfMale</dt>
        <dd>Percent of males among homeless spots residents.</dd>
</dl>
