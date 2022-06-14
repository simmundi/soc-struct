## TRF file format

TRF file contains **metadata** for reconstructing the entire engine state,
i. e. list of entities and their components. It is inspired
by VNF - the file format used by VisNow.

The TRF file itself does not contain the data; instead it describes:

- **structure**, including the definition of actual components used by the engine
- **references** to sources of data (for now - files, but other sources are possible in the future).

The structure is described by qualified names of Java classes defining the components.
The mappers for the components are instantiated through reflection.

The references to data sources are names of separate files and their formats. Each file contains
one uncompressed column of data.

### TRF

There's no schema file, but the structure of the XML is simple enough.

All the tags reside in the namespace `https://icm.edu.pl/ns/trf/1.0`

The whole structure is wrapped into `data` tag with `count` attribute (number of entities).

The structure consists of `component` tags, one per defined component. Components have
`count` and `type` attributes. The latter is the fully qualified class name of the 
Component. The existence of the former allows for slight optimization in cases when a component
 is only non-empty for a couple of first values; in the future, we could introduce a `firstId` or `offset` attribute.

Components contain `attributes` - descriptors of the actual data to load.
Each attribute has (for now) three attributes:
- `name` allows engine to bind the loaded data to the correct component attribute
- `format` describes the format of the file; for now it can be `boolean`, `byte`, `double`,
`entity_string`, `entity_list_string`, `enum`, `float`, `int`, `short`, `string` (the entity formats are the same string formats used 
for CSV files, i.e. id in base36 or list of comma-separated ids in base36).

An example structure containing:
- 99 entities
- three components: `Named`, `Location`, `Person`
- five data files
Would be described as follows

```
<data count="99" xmlns="https://icm.edu.pl/ns/trf/1.0">
    <component type="Named" count="99">
        <attribute name="name" file="dane_named_name.bin" format="string"/>
    </component>
    <component type="Location" count="99">
        <attribute name="n" file="dane_location_n.bin" format="int"/>
        <attribute name="e" file="dane_location_e.bin" format="int"/>
    </component>
    <component type="Person" count="99">
        <attribute name="sex" file="dane_person_sex.bin" format="enum"/>
        <attribute name="age" file="dane_person_age.bin" format="int"/>
    </component>
</data>
```

### The future

Using TRF files allows us to evolve storage separately from the program logic.
Some ideas for the future would be:

- using a single HDF5 / Parquet to store the actual data (mapped by TRF)
- using CSV to store the data (mapped by TRF), to make them immediately human-redeable and
usable across contexts
- storing data in a database

### Issues

Separate evolution of code and data _should_ be made simpler by using TRF files.
However, certain issues will persist, either because they seem to be unavoidable or 
because of lack of better ideas:

- There's no static guarantee that columns defined in TRF are the exact columns
required by the component (adding / removing attributes to components will cause
problems)
- no clear idea how to evolve components

### Why not just CSV?

CSV was used at the beginning and works surprisingly well. The problems are:

- no way to discern types from the shapes of the columns; e.g. is "teryt" a string or an int?
- no way to store information about the components used - a series of `require` calls
must be made before loading. Forgetting a single `require` means data loss.
- whole file must be loaded and parsed, even if a small subset is needed.
