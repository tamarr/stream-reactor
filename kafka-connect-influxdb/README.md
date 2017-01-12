# InfluxDB KCQL

The KCQL syntax of the InfluxDB sink is:

    INSERT INTO <measure> SELECT <fields> FROM <source topic> <PK?> WITHTIMESTAMP <field_name>|sys_time()

This allows us for example to sink all records from a topic into a InfluxDB time-series, injecting the current timestamp:

    INSERT INTO influxMeasure SELECT * FROM influx-topic WITHTIMESTAMP sys_time()

or select a particular field as a `timestamp`

    INSERT INTO influxMeasure SELECT * FROM influx-topic WITHTIMESTAMP ts

To define values of fields to become `indexed` in influx, use the `PK` syntax:

Example:

    INSERT INTO influxMeasure SELECT actualTemperature,targetTemperature FROM influx-topic PK machineId,type WITHTIMESTAMP ts

The above KCQL would translate into:

    INSERT temperature,machineId=1,type=boiler actualTemperature=30,targetTemperature=32

## How InfluxDB indexes work

Data in influxDb is organised in time series where each time series has `points`,
one for each discrete sample of the metric. Points consists of:

* `time`: the timestamp
* `measurement` : which conceptually matches the idea of a SQL table
* `tags` : key-value pairs in order to store index values, usually metadata.
* `fields` : key-value pairs, containing the value itself, non indexed.

null values aren’t stored.

The structure is of the data is:

    measurement,tagKey1=tagVal1,tagKey2=tagVal2 fieldK1=fieldV1,fieldK1=fieldV1

## Infux Tags

Tags are optional. You don’t need to have tags in your data structure, but it’s generally a good idea to make use of them because,
unlike fields, tags are indexed. This means that queries on tags are faster and that tags are ideal for storing commonly-queried metadata.
