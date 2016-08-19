#!/bin/bash

# replaces epidemap_dev databse with epidemap_demo

/usr/pgsql-9.3/bin/pg_dump --username=postgres epidemap_demo --format=custom > epidemap_demo.dump

/usr/pgsql-9.3/bin/pg_restore --username=postgres --dbname=epidemap_dev --clean --no-owner < epidemap_demo.dump