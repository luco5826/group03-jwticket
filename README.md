# Group 03 - JWT Ticket

This repository contains the projects that implement specifications for Lab02.

## Stateless validator

Each ticket is validated against the token only, therefore raising errors in these cases:

- Token expired: the `exp` field is before the current date
- Wrong signature: the token has been manipulated
- Unsupported zone: the zone provided with the ticket is not in the validity zones (`vz`) of the ticket

### USL

The plot of the behavior of the application under different load is the following

![](./server/src/main/kotlin/it/polito/wa2/group03/server/benchmark/statelessConcurrencyPlot.svg)

As it can be seen the plot stays more or less constant even at 256 concurrent clients. This is due to the fact that the
only bottleneck of this implementation is the speed of the processor the server is run on

## Stateful validator

Each ticket is validated with the same process of the stateless version but in addition it is forbidden to validate the
same ticket twice. Hence the validation requires to check against all the validated tickets if the current one is
already present (each ticket is identified by the `sub` field)

### USL

The plot of the behavior of the application under different load is the following

![](./server/src/main/kotlin/it/polito/wa2/group03/server/benchmark/statefulConcurrencyPlot.svg)

This version rapidly decreases just after 16 concurrent clients. This is due to the fact that with the increasing number
of requests, the number of validated tickets increases too and that the single source of truth for the already validated
tickets is a shared object that needs to be locked by each thread.

