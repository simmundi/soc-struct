#Algorithm

## Recreate basic household structure

### Establish the household size distribution

Uses:

- CoVID_dane_02_mieszkania.TXT (A)

Data from the file is anonymized (among other ways) by denoting households larger than 9
as just "9+", without the actual size.

In this step, we fit a gamma distribution (using the simplex algorithm) to the distribution of family sizes from the GUS model to establish parameters needed to predict sizes of 9+ households when needed.

### Build households and actors

Uses:

- eurostat age / sex structure (A)
- CoVID_dane_01_osoby.TXT (B)
- CoVID_dane_02_mieszkania.TXT (C)
- Distribution parameters established in the former step (D)

One large pool of people of all ages is created, based on the Eurostat data ( A ).

For each poviat, two pools are created:
- one for ages (binned in 5 year increments)
- one for sex (two bins, male and female)
- Sizes of the pools come directly from the GUS model ( B ).

The algorithm iterates over households from the GUS model ( C ); for each record,
a set of entities is created:
- a household, immediately placed in the poviat (but without precise location)
- family members - their number is based on the data from ( C ), i.e. counts of people
- between 0-65, 65-70, 70-75, 75+, but for households larger than 9 the count is extended using gamma distribution ( parameters taken from D )

The ages and sexes of the family members are picked from the poviat-specific pools without replacement.
Since the ages from the GUS model are binned by 5, they are further refined using
the eurostat pool (a draw with replacement), based on both age and sex.
This last step introduces some correlation between age and sex, which is absent from the GUS model.

### Clone households

Uses:

- powiats_population.csv (A)
- replicant configuration (B)
- agents and households created in former step (C)
- eurostat age / sex structure (reuses the pool from the last step) (D)
- 
Population in poviats ( A )  is aggregated and a ratio of replicants
(i.e. agents to be created later, taken from B ) to the total is calculated.

For each poviat, the total target population is considered by taking the GUS target population
and subtracting the replicant count, based on the replicant ratio (e.g. if a poviat has GUS population of 100 000
and the replicants-to-standard-agent ratio is 0.01, then the target
count of basic agents for the poviat is (1 - 0.01) * 100 000).

Also for each poviat, pools of households are created, categorized by a histogram
of their age structure (where age is binned by 5 year increments,
just like in the GUS model); e.g. a family of two children: 2,3, and three adults of 23, 26, 72
would be represented by a vector [2, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 ...])

Then new families are created in each poviat, by picking without replacement from
the households pool and creating a family of the same shape as the selected one.
As before, the ages are refined using eurostat age structure.

Creation of new families is repeated until the number of agents in the poviat is equal
to the target population or bigger.

The households created in this step come from outside the GUS model,
but are directly influenced by it; the intended outcome is that this procedure
changes the number of households, but does not affect their distribution in relation
to the shapes described by the GUS model.

### Decorate actors

Uses:

- names_f, names_m, surnames
- agents created in the former step

Pools are created for male and female names and surnames ( A ).

The algorithm iterates over households and - in an inner loop - over members of each.
A surname is picked for each household; a name (based on sex) for each actor.
After assigning name to each actor, there is a small probability that the surname
within the household will also change.

### Place households in kilometer grid

Uses:

- dane_02 (GUS)
- gminy.dat
- data from the former step

For each poviat a pool of households is created, categorized by the count of members,
clamped to [3, 10], a flag whether the household includes a 75+ member
and a kilometer grid cell of the household.
The association between grid cells and poviats is done using ( B ),
the association between the household type and the grid - using ( A ).

The algorithm iterates over the households in each poviat and - based solely on
the categorization of the family (the count and the flag) picks the kilometer cell.
The current household is then given a precise location, which is a uniformly random point within the selected kilometer cell.

Caveats:
- the picking is currently with replacement; it should be examined whether it can be changed to without.
- In rare cases, the exact shape cannot be found in the given poviat - in those cases the 75+ flag is ignored, and the number of such violations - reported.

### Adjust households to Address Points

uses:

- households created up to this point
- address point database

For each household, we uniformly select an address point within its kilometer grid cell.

## Create Educational Institutions (EIs)

### Recreate real institutions

uses:

- lista_placowek_oswiatowych.csv
- address point database
- postal code database
- gminy.dat

The algorithm iterates over the data from the government - a csv file enumerating actual institutions,
their addresses, types and sizes.

Each institution is classified as either a kindergarten, primary school, high school
or a school complex (zespół szkół);

The address of each institution is geodecoded using the address point database.
For most institutions, their exact address point is found;
for some, a location is selected randomly based on the postal code to PL1992 cell mapping,
or - failing that - within the proper commune.

An entity is created for each institution, containing level, target number of pupils,
teachers and the exact place.

### Recreate institutions from pdyn

uses:

- jew.dat, djew.dat 

The algorithm creates universities based on the original data files from PDYN. The current data model distinguishes between "large" and "small" universities, labelling them as "djew" or a "jew".

### Assign Attendees to EIs

uses:

- data created in the former steps (educational institutions)
- configuration settings

The algorithm works in five similar passes. Each pass is parametrized with:
- the level of Educational Institution;
- age of the actor eligible for the level of education;
- strategy of attendance: whether the choice of the specific EI happens on a per-household or a per-person basis;
- allowed radius (configurable through properties file).

<table>
<thead>
<tr>
<th>age</th>
<th>strategy</th>
<th>levels</th>
<th>radius*</th>
</tr>
</thead>
<tbody>
<tr>
    <td>[2, 7) </td>
    <td>per-household</td>
    <td>Kindergarten</td>
    <td>25</td>
</tr>
<tr>
    <td>[7, 15) </td>
    <td>per-household</td>
    <td>Primary school, school complex</td>
    <td>25</td>
</tr>
<tr>
    <td>[15, 20)</td>
    <td>per-household</td>
    <td>High school, school complex</td>
    <td>25</td>
</tr>
<tr>
    <td>[20, 26)</td>
    <td>per-person</td>
    <td>University</td>
    <td>50</td>
</tr>
<tr>
    <td>[20, 26)</td>
    <td>per-person</td>
    <td>Big University</td>
    <td>50</td>
</tr>
</tbody>
</table>
   
At the beginning of each pass, a pool of free slots categorized
by eligible institutions is created.
The pool is additionally mapped by location - each kilometer grid cell is 
associated with a subset of the pool, containing only institutions within the given radius from the cell.

The algorithm iterates over households and picks agents eligible for becoming attendees
(for example for the primary schools pass, the age must be between 7 and 14).
A slot is picked without replacement from the subset of the IE pool
accessible from the household location (i.e. from the subset of IEs within the allowed radius).

Depending on the strategy (per-household or per-actor), either
all the agents are assigned to the same IE (all children 3-6 in one household
are assigned to a single kindergarten)
or the selection is repeated for each agent
(two adults from the same household might attend two different universities).

The radius shown is the default value - but is configurable.

## Workplaces and employees

### Fitting company distributions

uses:

- hard-coded constants

Two Zipf distributions of company sizes are fitted to statistical data:
- sizes of companies in the private sector are fitted to:
-- total count of companies, counts of micro- mini- mid, large and huge companies;
-- counts of employees: the self-employed, employed in microcompanies (sizes 2-5),
-- total number of people employed in the private sector.
- sizes of companies in the public sector are fitted to:
-- number of public sector companies
-- number of people employed in the public sector companies

**caveat:** we theorize that the zipf distribution reflects sizes of companies,
especially since we separate the public and the private sector.
However, what is really created at this stage are not really companies,
but rather "locations of employment".
We do not try to model entities like "Żabka Polska Sp. z O. O.",
but rather all the Żabka stores separately.

### Assigning companies to gminas and employees

Uses:

- employment-specific flows between communes (GUS) (A)
- list of gminas (wikipedia) (B)
- distributions calculated in the former step (C)
- formerly created entities (D)
- hard-coded parameters (E)

For each gmina a set of values is calculated:
- The number of eligible employees is found using a hard-coded formula:
  _age >= 19 and age < retirement age_, where _retirement age_ is 60 for females and 65 for males ( D, E ).
- The number of employees leaving the gmina to work in any other gminas ( A )
- The number of employees coming to the gmina from other gminas to work ( A )

By multiplying the number of eligible employees in each gmina by the global employment rate,
subtracting the outflux and adding the influx of employees,
we can then determine the number of slots in all the workplaces needed in the gmina.

A pool is created of all the slots, categorized by gminas.

Distributions for the workplace sizes created in the former step are added to form one distribution;
companies with the size of 1 are removed (as they likely represent b2b)
and their count is evenly distributed among companies of sizes 2-49
(so 50 companies of size one can become, for example, two companies of size 25).

The resulting list of workplace sizes is used to create workplace entities,
localized in gminas using the bin pool
(the companies do not have an exact location at this stage).

For each gmina, three bin pools are created:

- employment condition: pool categorized by employment condition, one of three options:
  **employed locally**, **unemployed**, **employed outside of the commune** (with proper counts);
- slots in local workplaces - categorized by workplaces (each being local to the commune)
- slots in workplaces in different gminas - categorized by gmina

The algorithm iterates over eligible workers (using same age / sex criterion as before) and:

- picks (without replacement) a condition for the actor;
- if the condition selected is unemployed, the algorithm continues with the next actor
- if the condition selected is working locally, then a local company is picked (without replacement) from the second pool and assigned to the agent
- if the condition selected is working outside of the commune - then the target commune is picked (without replacement) from the third pool, then the slots-in-local-workplaces pool of the target commune is used to select (without replacement) the workplace for the agent

### Adjust workplaces to Address Points

Uses:
- address point database
- formerly created workplaces (placed with gmina granularity)

For each workplace, the algorithm uniformly selects an address point within its gmina.

## Special agents, aka Replicants

The main steps of creating the social structure are organized in layers;
it is assumed that sex, age, clustering into households, assigning to educational institutions,
employment etc. are mostly orthogonal.

In real life, we believe the above holds true for the majority of the population.
However, there are groups of people where age / sex / clustering / employment / education
are strongly bound. We call these agents - replicants, and they require a separate process to create,
since they are created along with their "household" (the word household has a specific meaning in this context;
it reuses the household structure from the data model, but it might represent
an actual household, a room in a nursing home, a living area in barracks, etc.).

### Create common pool for replicants

uses:

- formerly created agents

- A pool of people (sex / age range) is created, based on people in voivodship Wrocławskie.

### Barracks

uses:

- replicant pool
- population density data
- configuration (defaults in brackets): total count of soldiers (50000), room size (25), number of rooms in single location (40)

A pool of soldiers is created, by selecting a subpool of the replicant pool,
containing people aged 20-49.

While the number of replicants to create is bigger than the number of replicants created, the algorithm:

- selects a location, by picking a random populated kilometer grid cell;
- selects total count of replicants in the location, from the allowed range (based on maximum number of rooms and room size)
- creates all the rooms as households
- for each room, creates the configured number of replicants
  by drawing statistics from the pool of soldiers

### Clergy houses

uses:

- replicant pool
- population density data
- configuration (defaults in brackets): count of clergymen (16000), room size (2)

A pool of clergymen is created, by selecting a subpool of the replicant pool, containing males aged 25-80.

While the number of replicants to create is bigger than the number of replicants created,
the algorithm:

- selects a location, by picking a random populated kilometer grid cell;
- creates a single households, representing the clergy house;
- creates the configured number of replicants by picking statistics without replacement from the pool of clergymen

### Dorm houses

uses:

- replicant pool
- population density data
- pre-created university entities
- configuration (defaults in brackets): total count of students in dorms (90000), dorm room size (4), number of dorm rooms in a single dorm house (100); maximum distance to the university (10)

A pool of students is created, by selecting a subpool of the replicant pool, containing people aged 20-24.

A pool of student slots is created, categorized by universities (jews, djews) and mapped by kilometer grid cells.

While the number of replicants to create is bigger than the number of replicants already created, the algorithm:

- selects a location by sampling the student slots pool to choose a university, and then picking a random,
  populated kilometer grid cell within the defined radius
- selects total count of students in the location, from the allowed range
  (based solely on maximum number of rooms and room size)
- creates all the dorm rooms, as households
- for each dorm room, creates the configured number of students by drawing from the student pool
- for each student, a university to attend is selected by picking, without replacement,
  slots available in the cell (within the radius); this means that while a dorm house is placed
  in proximity of one, defined university, its inhabitants can attend any university in the area

### Homeless

Creates households which emulate spots frequented by the homeless (like old buildings, makeshift structures etc) - not shelters.

uses:

- replicant pool
- population density data
- configuration (defaults in brackets): total count of the homeless (50000), spot size (5),
  number of such spots per square kilometer (10), percent of males (87)

A pool of ages for homeless is created (between 20 and 80).

While the number of replicants to create is bigger than the number of replicants already created, the algorithm:

- selects a location, by picking a random populated kilometer grid cell;
- selects total count of homeless in the location, from the allowed range (based on maximum number of spots and spot size)
- creates all the configured spots as separate households
- for each spot, creates the configured number of replicants by drawing age statistics
  from the pool of the homeless and forcing selection of sex to fit the parameters
  (the selected age and sex are drawn from the general replicant pool but not replaced).

### Monasteries

uses:

- replicant pool
- population density data
- configuration (defaults in brackets): total count of monks / nuns (32000),
  room size (2), number of rooms in single location (50)

A pool of monks/nuns is created (ages between 20 and 80) is created from the replicant pool.

While the number of replicants to create is bigger than the number of replicants already created,
the algorithm:

- selects a location, by picking a random populated kilometer grid cell;
- selects total count of replicants in the location, from the allowed range (based on maximum number of rooms and room size)
- creates all the rooms as separate households;
- for each room, select sex (for inscrutable reasons, sex is not selected for the entire monastery)
  and create the configured number of replicants, drawing the age statistics from the pool.

### Nursing Homes

uses:

- replicant pool
- population density data
- configuration (defaults in brackets): total count of nursing home patients (110000);
  patients per room (10); maximum number of rooms per location (25)

A subpool is created from the replicant pool, containing people aged 70+ of both sexes.

While the number of patients to create is bigger than the number of patients created, the algorithm:

- selects a location, by picking a random populated kilometer grid cell
- selects total count of replicants in the location, from the allowed range (based on maximum number of rooms and room size)
- creates rooms as separate households
- for each room, creates the configured number of replicants,
  drawing both age and sex from the prepared pool, without replacement.

## Prisons

uses:

- replicant pool
- population density data
- configuration (defaults in brackets): total count of penitents (85000);
  penitents per cell (6); maximum number of cells per prison (167)

A subpool is created from the replicant pool, containing people aged 20 and above, of both sexes.

While the number of replicants to create is bigger than the number of replicants already created,
the algorithm:

- selects a location, by picking a random populated kilometer grid cell
- selects total count of replicants in the location,
  from the allowed range (based on maximum number of cells and cell size)
- creates cells as separate households
- for each cell, creates the configured number of penitents, drawing both age and sex from the prepared pool, without replacement.
