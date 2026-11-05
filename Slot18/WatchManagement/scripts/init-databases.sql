-- [TODO-10.2] Create only the three service databases; Hibernate owns each schema.
IF DB_ID(N'DBAccount') IS NULL
    CREATE DATABASE DBAccount;
GO
IF DB_ID(N'DBBrand') IS NULL
    CREATE DATABASE DBBrand;
GO
IF DB_ID(N'DBWatch') IS NULL
    CREATE DATABASE DBWatch;
GO
