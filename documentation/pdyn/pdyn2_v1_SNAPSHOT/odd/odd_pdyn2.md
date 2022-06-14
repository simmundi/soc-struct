<!--  # The Overview, Design concepts and Details Protocol of the ICM New Epidemiological Model (pdyn 2) -->

# The Overview of the ICM New Epidemiological Model (pdyn 2) [Draft] 

This document describes the second epidemiological model developed at the Interdisciplinary Center for Mathematical and Computational Modelling at the University of Warsaw, Poland (ICM New Epidemiological Model a.k.a. pdyn 2)[^1]. The ICM New Epidemiological Model is the detailed spatially and contextually resolving agent-based model (ABM) of airborne diseases spreading, utilizing the representation of the social structure of Poland. 

[^1]: The model and this document were created as part of the "ICM Epidemiological Model development" project, funded by the Ministry of Science and Higher Education of Poland with grants 51/WFSN/2020T and 28/WFSN/2021 to the University of Warsaw.

The model description follows the Overview, Design concepts, Details (ODD) protocol for describing individual- and agent-based models @grimmStandardProtocolDescribing2006, as updated by Grimm et al. @grimmODDProtocolDescribing2020.

PDYN is a highly realistic model of spread of the COVID19 pandemic throughout Polish society. 38 millions of agents interact both in 2D space and through network of social interactions - in families, at work, in schools, kindergartens and universities.

PDYN is an interdisciplinary effort - it can be seen as a comprehensive Agent Based Model formalization of expert knowledge from diverse fields: virology (model of transmission and progression of sickness, ), sociology and geography (models of interaction within society, actual network of schools, businesses, social structure).

## Purpose and patterns

### Purpose

The primary purpose of pdyn2 is to investigate (describe and explain) the spatial and temporal dynamics of airborne diseases spread across the defined (and in particular Polish) society. The model predicts the dynamics of the number and location of disease-related states of agents in response to specific changes in the properties of the pathogen and the social structure and behavior. 

Our modeling approach takes into account the following factors influencing the course of the epidemic:
- Geographically distributed social structure.
- Level of contact in the places where people are present and meeting daily (called contexts).
- People's mobility.
- Virus sowing.
- Virus infectivity.
- Severity and time course of the disease.
- Introduction of new pathogen variants.
- Cross-immunity.
- Testing.
- Immunization (natural and by vaccinations).

In particular, the model aims to test to what extent such conditions can explain the number and location of subjects who are: susceptible, latent, actual and identified cases, infectious symptomatic, infectious non-symptomatic, hospitalized, requiring an ICU stay, recovered, and deceased; At the society (or its parts) level the factors mentioned above are meant to explain: effective reproduction number, rate of replacing variants, and (detected and actual) immunoprevalence.

The secondary goal is to test theoretically-derived hypothetical scenarios to elucidate the mechanisms of pathogen spread across society.

The ultimate objective of the model is to forecast potential trajectories of epidemic development and simulate various scenarios, assuming the following constraints: the appearance of new variants, vaccinations programs, specified epidemic mitigation strategies (administrative restrictions and/or spontaneous changes in social behavior) such as quarantine, school closures, local and dynamic lockdowns, wearing masks and keeping physical distance, or other social changes related to the change in contact (e.g., holidays). The practical goal is to support administrative decisions during pandemics. Therefore, the model has features that enable meticulous fitting of the real-world pandemic in the given society.

### Patterns

1. Recreation of the epidemic curve (_epi curve_).
2. Recreate epidemic patterns according to their manner of spreading through a population: point source, continuous or intermittent source, and propagated.
3. Obtaining the impact effect of the average size of households on the course of the epidemic.
4. Recreation of flattening of the curve effect dependent on constraints imposed to contact rates served to mimic, e.g., non-pharmaceutical interventions (e.g., strong suppression of the epidemy leads to multiple and long waves; on the contrary, 'wild' / not suppressed epidemic gives typically single and short wave).
5. Recreation of flattening of the curve effect depends on mobility constraints.
6. Obtaining the effect of flattening of the curve after introducing social bubbles
7. The reconstruction of the displacement of less transmissible virus variants by the more transmissible ones.
8. Restoration of the decoupling effect of infections and severe disease courses due to immunization.
9. The recreation of the emergence of herd immunity achieved naturally and by vaccination.
10. Obtaining spatial differentiation in the number of cases and hospitalizations depending on the different immunization.
11. Obtaining differentiated protection against hospitalization and death based on different characteristics of the course of infection with virus variants (different state-time tables)
12. The recreation of the emergence of local epidemic epicenters due to superspreaders.
13. The recreation of the delay in the outbreaks in villages relative to cities.
14. Possibility to accurately recreate the course of the real-world epidemic on national and regional scales.

## Entities, state variables, and scales


### Entities

<!-- what the entities represent, why they are in the model, and whether the model has one, several, or many entities of each kind. -->

The following kinds of entities are included in the \pdyn{} model: agents, contexts, and environment. 


<!-- Describe whether the model represents space, and how. Not all ABMs are spatial; some do
not represent space or the location of agents. The discussion of spatial scales should therefore
start by saying whether space is represented. This discussion should also say how space is
represented. First, state whether the model is one-, two-, or three-dimensional. Many ABMs are
two-dimensional and represent space as a collection of discrete units (“cells” or “patches”), so
the location of an agent is defined by which spatial unit it is in. These units are often square grid
cells, but it is also common to use hexagonal cells and irregular polygons. Space can also be
represented as continuous, with locations described using real numbers. Both discrete and
continuous space can be used in the same model. -->

_Virtual space_. The \pdyn{} is a spatially explicit model, yet, its spatial aspects are represented in mixed mode. The basic structure of the model is networked in a non-geographic manner. The closeness of agents and contexts is reflected in the network based on the input data, which is the virtual society. However, some points of interest (particularly households) have assigned a geographic location in a two-dimensional continuous space defined by the territory of the virtual society data. Moreover, the model's representation of streets and (semi-)public places is spread over a 1x1 km grid corresponding to the territory defined by input data. Subsequently, the territory can be projected on the territory and divided into smaller units.  

_Agents_. The model's basic entities are _agents_ representing members of society. In particular, there are 38,431,887 agents implemented in the model of Polish society, reflecting the distribution of Polish inhabitants in 2019.

_Collectives_. The second key entities of the model are _contexts_, denoting permanent or temporal locations of agents' being together. Six types of contexts are implemented in the model. <!-- (The number of contexts of a given type implemented in the model for Polish society is given in parentheses.) -->
- _Households_ representing the site of permanent accommodation <!-- ($n$ = 12,321,206)-->. 
- _Workplaces_ representing working sites<!-- ($n$ = 12,321,206).--> <!-- - _Kindergartens_ ($n$ = 12,202)-->. 
- _Schools_<!-- ($n$ = 19,325)--> as places of basic education (including kindergartens).
- _Universities_ as sites of study<!-- ($n$ = 426)-->.
- _Big universities_ as large universities with spacious campuses<!-- ($n$ = 426)-->.
- _Streets_ covering streets and public and semi-public sites (commercial services, shops, etc.). Instead of one catch-all street context, there are multiple to mimic cross-generational aspects of contact in such places. Aspects are age groups that mix (and infect each other). There are ten types of _street_ context representing ages binned by ten: 0-10, 10-20, 20-30, 30-40, 40-50, 50-60, 60-70, 70-80, 80-90, 90-100 (named respectively: street_00, street_10, ..., street_90)<!-- ($n$ = 426)-->.

<!-- Environment. Many models include a single entity that performs functions such as
describing the environment experienced by other entities and keeping track of simulated
time. Such an entity can be referred to as the environment, or by using the terms used by
modeling platforms for the software object that performs these functions (e.g., the
“Observer” in NetLogo (Wilensky 1999), the “model”, “model swarm”, etc., in other
platforms). Even if this entity does not represent anything specific in the modeled system,
it can be included in ODD if useful for describing the model. Any global variables
essential to the model, such as those describing the environment, should be associated
with such an entity. -->

_Environment_. The model environment reflecting the conditions of the pandemic can be divided into three domains: global variables (time), pathogen and disease properties (virus transmissivity, times and probabilities of disease stages, cross-immunity, seeding) and human behaviour properties (contacts in context, travelling, testing, self-isolation, quarantine). 
The environment consists of the _base properties_ (the state at the outbreak onset) and the _scenario properties_ describing the changes during the epidemic.


<!-- Provide the rationale for the choice of entities. Choosing which kinds of entities to include in
a model, and which to leave out, is a very fundamental and important design decision. Often this
choice is not straightforward: different models of the same system and problem may contain
different entities. Therefore, it is important for making your model scientific instead of arbitrary
to explain why you chose its entities. (Pattern-oriented modeling, discussed at Element 1, is a
strategy for making this choice.) -->

<!-- Why those entities were included and, if relevant, why other entities were not included. -->

### State variables

<!-- The second part of this element is a description of the state variables (sometimes called
“attributes”) of each kind of entity. State variables of an entity are variables that characterize its
current state; a state variable either distinguishes an entity from other entities of the same kind
(i.e., different entities or agents have different values of the same variable), or traces how the
entity changes over time (the value of an entity’s variable changes over simulated time). -->


#### Agents' attributes

The attributes of agents come in seven groups: 
- Personal data.
- Data about contexts.
- Data about travel.
- Data on the current disease.
- Immunization data.
- Data about testing.
- Data about general agent behaviour mode.

Agent state variables are described in [Table 1](#agent_attributes).


<a name="agent_attributes"> Table 1. Agent state variables. </a>

|Name|Meaning|Units|Dynamics|Type|Range|
|----|-------|:---:|:------:|----|-----|
|Personal data      |
|`ID`               |ID of agent.                                   |nom[^2]|S   |Text     |{agents IDs}            |
|`sex`              |Sex of the person.                             |nom    |S   |Text     |{"M","F"}                |
|`age`              |Age of the person.                             |years  |S   |Integer  |[0--100]                 |
|Context data       |
|`homecontext`      |The household in which the agent lives.        |nom    |S   |Text     |{households IDs}         |
|`contexts`         |List of sites where a person meets other people on daily basis.                                                              |nom    |S   |Text*[^3]|{contexts IDs}         |
|Disease data       |
|`infection_stage`  |Agent's current infection state.               |nom    |D   |Text     |{"latent"; "infectious asymptomatic"; "infectious symptomatic"; "hospitalized outside ICU"; "hospitalized before ICU"; "hospitalized at ICU"; "dead"; "recovered"} |
|`infection_time`   |Day of infection.  |days   |D   |Integer  |>= 0                     |
|`infection_load`   |A variant of the virus that the person is currently infected with.                                                               |nom    |D   |Text     |{variants IDs}           |
|Behavior data      |
|`behavior_type`    |A pattern of a person's behavior in terms the contexts in which the person stays during the day.                                                                |nom     |D   |Text     |{"dormant"; "routine"; "hospitalization"; "private_travel"; "selfisolation"; "quarantine"; "death"} |
|Travel data        |
|`travel_day`       |Day of travel.                                 |days    |D   |Integer  |>= 0                    |
|`stays_at`         |Household to which the agent travels.          |nom     |D   |Text     |{households IDs}        |
|Immunization data~--- list of events  |
|`event_time`       |Day of immunization.                          |days   |D   |Integer  |>= 0                     |
|`event_load`       |Variant of immunization.                       |nom    |D   |Text     |{variants IDs}           |
|Testing data~--- list of records     |
|`record_day`       |Day of the test                                |days   |D   |Integer  |>= 0                     |
|`record_type`      |Result of the test.                            |nom    |D   |Text     |{"positive"}             |

[^2]: The designation of nom as a unit means that the measurement level is nominal, and nominal categories are the units.
[^3]: Astaerisked (*) are data types, which are lists of records containing the specified variables.


#### Contexts' attributes

Context state variables are given in [Table 2](#context_attributes).

<a name="context_attributes"> Table 2. Context state variables. </a>

|Name|Meaning|Units|Dynamics|Type|Range|
|----|-------|:---:|:------:|----|-----|
|`id`              |ID of the context.                     |nom[^2]     |S   |Text    |{Numeric IDs} |
|`contex_type`     |Type of the context.                   |nom         |S   |Text    |{"household"; "workplace", "school"; "university"; "big_university"; "street_00"; "street_10"; "street_20"; "street_30"; "street_40"; "street_50"; "street_60"; "street_70"; "street_80"; "street_90"} |
|`context_location`|                                       |coordinates |S   |Float, Float  | specified by input |


#### Environmental variables

Environmental variables come in  groups: 
- Timer.
- Contexts properties. 
- Transmission parameters.
- Sowing data.
- Cross-immunity data.
- Disease state-time table.
- Travel parameters.
- Data about testing. 
- Data about agent behavior patterns.

The model environment reflecting the conditions of the pandemic can be divided into three domains: global variables (time), pathogen and disease properties (virus transmissivity, times and probabilities of disease stages, cross-immunity, seeding) and human behaviour properties (contacts in context, travelling, testing, self-isolation, quarantine).


Model state variables are given in [Table 3](#model_attributes).

<a name="model_attributes"> Table 3. Model state variables. </a>

|Name|Meaning|Units|Dynamics|Type|Range|
|----|-------|:---:|:------:|----|-----|
|Timer                     |
|`current_date`                 | Actual date in simulation.                     |days       | D | Integer | >= 0 |
|Contexts properties                  |
|`household_base_weight`        | Base contact rate in households.               |(hours)    | S | Float   | >= 0 |
|`workplace_base_weight`        | Base contact rate in workplaces.               |(hours)    | S | Float   | >= 0 |
|`schools_base_weight`          | Base contact rate in schools.                  |(hours)    | S | Float   | >= 0 |
|`university_base_weight`       | Base contact rate in universities.             |(hours)    | S | Float   | >= 0 |
|`biguniversity_base_weight`    | Base contact rate in large universities.       |(hours)    | S | Float   | >= 0 |
|`street_base_weight`           | Base contact rate in common places.            |(hours)    | S | Float   | >= 0 |
|`household_weight_modifier`    | Contact rate in households modifier.           |fraction   | D | Float   |[0--1]|
|`workplace_weight_modifier`    | Contact rate in workplaces modifier.           |fraction   | D | Float   |[0--1]|
|`schools_weight_modifier`      | Contact rate in schools modifier.              |fraction   | D | Float   |[0--1]|
|`university_weight_modifier`   | Contact rate in universities modifier.         |fraction   | D | Float   |[0--1]|
|`biguniversity_weight_modifier`| Contact rate in large universities modifier.   |fraction   | D | Float   |[0--1]|
|`street_weight_modifier`       | Contact rate in common places modifier.        |fraction   | D | Float   |[0--1]|
|Transmission data              |
|`transmissibility`             | Trasmissibility of the basic variant of virus. |?          | S | Float   | > 0  |
|`relative_transmissibility`    | Trasmissibility of virus variants relative to the basic variant.                                                                    |fraction    | S | Float*  | > 0  |
|Sowing data~--- list of lists per variant[^4] |
|`sowed_agent`                  | ID of the agent being sowed.                   |nom        | S | Text    | {agents IDs} |
|`sowed_symptomatic`            | Is agent being sowed symptomatic?              |nom        | S |bool     | {0, 1}  |
|`sowed_icu`                    | Is agent being sowed requiring stay on ICU?.   |nom        | S |bool     | {0, 1}  |
|`sowed_days`                   | Day of being infected.                         |day        | S |Integer  |  ℤ  |
|Cross-immunity data~--- list of lists per variant |
|`foo`             | Foo.                                                   |?          | S | Float   | > 0  |
|Disease state-time table~--- list of lists per variant per age bin |
|`disease_times`             | List of desease states durations.            |days       | S | Integer* | >= 0  |
|`lat_infa_probability`      | Probability of transition from latent to infectious asymptomatic stage.          |probability| S | Float |[0--1]|
|`lat_infs_probability`      | Probability of transition from latent to infectious symptomatic stage.           |probability| S | Float |[0--1]|
|`infa_rec_probability`      | Probability of transition from infectious asymptomatic to recovered state.       |probability| S | Float |[0--1]|
|`infs_hosp_probability`     | Probability of transition from infectious symptomatic to hospitalized outside ICU stage.    |probability| S | Float |[0--1]|
|`infs_bicu_probability`     | Probability of transition from infectious symptomatic to hospitalized before ICU stage.     |probability| S | Float |[0--1]|
|`infs_dea_probability`      | Probability of transition from infectious symptomatic to dead state.           |probability| S | Float |[0--1]|
|`infs_rec_probability`      | Probability of transition from infectious symptomatic to recovered state.           |probability| S | Float |[0--1]|
|`hosp_dea_probability`      | Probability of transition from hospitalized outside ICU to dead state.            |probability| S | Float |[0--1]|
|`hosp_rec_probability`      | Probability of transition from hospitalized outside ICU to recovered state.           |probability| S | Float |[0--1]|
|`icu_dea_probability`       | Probability of transition from hospitalized at ICU to dead state.            |probability| S | Float |[0--1]|
|`icu_rec_probability`       | Probability of transition from hospitalized at ICU to recovered state.            |probability| S | Float |[0--1]|
|Travel data                    |
|`travel_base_probability`      | Base probability of going on a trip.      |probability| S | Float   |[0--1]|
|`travelend_base_probability`   | Base probability of ending a trip.        |probability| S | Float   |[0--1]|
|`travel_probability_modifier`  | Probability of going on a trip modifier.  |probability| D | Float   |[0--1]|
|`travelend_probability_modifier`| Probability of ending a trip modifier.   |probability| D | Float   |[0--1]|
|Testing data                   |
|`test_base_probability`        | Base probability of being tested.         |probability| S | Float   |[0--1]|
|`test_probability_modifier`    | Modifier of probability of being tested.  |fraction   | D | Float   |[0--1]|
|Agents behaviour data          |
|`selfisolation_base_probability`| Probability of not staying at home while symptomatic.                                                                |probability| D | Float   |[0--1]|
|`selfisolation_probability_modifier` | Modifier of the probability of not staying at home while symptomatic.                                                                |fraction   | D | Float   |[0--1]|
|`quarantine_duration`          | The number of days of quarantine.         |days       | S | Integer | > 0  |


[^4]: Sowing data can be obtained from more basic specifications (for details, see Sowing submodel).


### Scales

<!-- The third part of this element is describing the model’s spatial and temporal scales. By “scales”
we mean the model’s extent—the total amount of space and time represented in a simulation—
and resolution—the shape and size of the spatial units and the length of time steps. It is important
to specify what the model’s spatial units and time steps represent in reality. A simple example
description of scales is: “One time step represents one year and simulations were run for 100
years. Each square grid cell represents 1 ha and the model landscape represents 1,000 x 1,000 ha;
i.e., 10,000 square kilometers”. Models often use different time scales for different processes, or
processes that are triggered only under certain conditions; such variations in scales should be
described here. -->

<!-- Describe scales in a separate paragraph. While it can be straightforward to describe spatial
scales while describing the model’s spatial entities (e.g., “Space is represented as a 100 by 100
grid of square cells that each represent 1 cm2 ”), we recommend writing a separate paragraph that
concisely states the spatial and temporal scales. These are fundamental characteristics of a model
that readers will often want to find easily. -->

<!-- When spatial or temporal scales are not fixed, describe typical values. -->

One time step represents one day, and simulations were run for an arbitrarily set number of days. 

The spatial scales of the model are defined by the input data (virtual society). For example, in the case of Poland, the model runs on the virtual territory of Poland in geographical coordinates; the implementation of street contexts on the grid with 1x1 km resolution requires a total of 800x800 cells. Poland's division comprises 16 voivodships and 380 poviats (administrative units).

<!-- Provide the rationales for spatial and temporal scales. In any kind of modeling, choosing the
scales is well-known as a critical design decision because model scales can have effects that are
strong yet difficult to identify. This is an especially important part of your model to justify, and
pattern-oriented modeling is again one strategy for doing so. -->

<!-- Why the spatial and temporal scales were chosen. -->


## Process overview and scheduling 



<!-- ## Design concepts

### Basic principles

### Emergence

### Adaptation

### Objectives 

### Learning 

### Prediction 

### Sensing

### Interaction 

### Stochasticity

### Collectives

### Observation 

## Initialization 

## Input data 

## Submodels -->
