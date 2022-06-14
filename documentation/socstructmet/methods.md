#Methods

(tbd)

## Homogeneous entities as the core data model

( tbd : technical merits)

## Different data models on different levels; versioning and transformations of the data models (within the core model)

( tbd : data modelling merits; DDD: walled gardens )

## Binned pools

Binned pools are our main operational tool:

they are pools of data items with differing categorizations,
built from real world data; used to iteratively draw data without replacement,
either in random fashion or by design, by drawing data from subpools
(e.g. from a pool of all the available ages, representing the demographic structure of Poland,
binned by exact age in years, we can sample a random age, sample an age larger than 20
or just draw - without replacement - an object with age of 27).

( tbd : the whys and hows )

Whenever a selection from a binned pool is mentioned in the algorithm selection, it should be considered a quasi-random (preseeded) draw without replacement.

