# Logical Class Diagrams

The original all-in-one class diagram was split by responsibility:

1. `01-application-overview.asta`: UI, application services, configuration, factories, and registries.
2. `02-collection-subsystem.asta`: collector abstraction, factory, implementations, and collection boundary models.
3. `03-preprocessing-subsystem.asta`: preprocessing chain and processor strategies.
4. `04-sentiment-subsystem.asta`: sentiment provider strategy, factory, and sentiment models.
5. `05-analysis-subsystem.asta`: analyzer strategy, registry, implementations, and output models.
6. `06-domain-and-storage.asta`: core domain objects, enums, and persistence abstraction.

Each file is an independent Astah project and can be presented separately.

## UML relationship notation

- Generalization: solid line with a hollow triangle pointing to the superclass.
- Realization/implementation: dashed line with a hollow triangle pointing to the rectangular `«interface»`.
- Association: solid line for a persistent reference between classifiers.
- Dependency: dashed arrow for transient use through parameters, return values, or local creation.
- Aggregation: hollow diamond at the owner whose parts can exist independently.
- Composition: filled diamond at the owner that controls the part's lifecycle.

The diagrams use aggregation and composition only where the source code establishes
an owner/part relationship; ordinary object references remain associations.
