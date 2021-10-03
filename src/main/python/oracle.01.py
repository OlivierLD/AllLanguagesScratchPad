#!/bin/python3
#
# Oracle ADB access
# First basic test.
# Requires a RACES user to have been created (see below).
#
# Important: Install InstantClient on your system:
# See https://www.oracle.com/database/technologies/appdev/python/quickstartpython.html#second-option-tab
# Use the DMG approach on MacOS
#
# Copy the unzipped wallet in <...>/Downloads/instantclient_19_8/network/admin
# mv Wallet_*.zip $HOME/Downloads/instantclient_19_8/network/admin
# cd $HOME/Downloads/instantclient_19_8/network/admin
# unzip Wallet_*.zip
#
from typing import List
import cx_Oracle

# path to libclntsh, on the machine this code runs on.
cx_Oracle.init_oracle_client(lib_dir=r"/Users/olivierlediouris/Downloads/instantclient_19_8")

connection : cx_Oracle.Connection = cx_Oracle.connect(user="RACES", password="AkeuCoucou_1", dsn="olivdbone_medium")
# print(f"Connection is a {type(connection)}")

cursor : cx_Oracle.Cursor = connection.cursor()
# print(f"Cursor is a {type(cursor)}")

# Create a table
cursor.execute("""begin
                     execute immediate 'drop table pytab';
                     exception 
                        when others then 
                           if sqlcode <> -942 then 
                              raise; 
                           end if;
                  end;""")
cursor.execute("create table pytab (id number, data varchar2(20))")

# Insert some rows
rows: List[tuple] = [
    (1, "First"),
    (2, "Second"),
    (3, "Third"),
    (4, "Fourth"),
    (5, "Fifth"),
    (6, "Sixth"),
    (7, "Seventh")
]
# print(f"Rows is a {type(rows)}")
# print(f"Rows[0] is a {type(rows[0])}")

cursor.executemany("insert into pytab(id, data) values (:1, :2)", rows)

# connection.commit()  # uncomment to make data persistent

# Now query the rows back
nb: int = 0
for row in cursor.execute('select * from pytab'):
    nb += 1
    # print(f"Row is a ${type(row)}")
    print(f"Row #{nb}: {row}")

print("Done!")
