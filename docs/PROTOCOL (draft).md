1. Handshake:






2. Message:

message level | length ---| message body -----------------------------------------------------------------------------------------|
| (integer) | (bytes array)                                                                                         |

method level              | length ---| method name | length ---| parameter1 ---| length ---| parameter2 ---| other parameters... |
| (integer) | (string)    | (integer) | (bytes array) | (integer) | (bytes array) |                     |


parameter | parameter body -------------------------------------------------------------------------------------|
| (bytes array)                                                                                       |

model     | length ---| field1 -------| length ---| field2 -------| length ---| field3 -------| other fields... |
| (integer) | (bytes array) | (integer) | (bytes array) | (integer) | (bytes array) |                 |

list      | length fo list | length ---| element1 -----| length ---| element2 -----| other elements... ---------|
| (integer)      | (integer) | (bytes array) | (integer) | (bytes array) |                            |

map       | length fo map | length ---| key1 ---------| length ---| value1 -------| other pairs... -------------|
| (integer)     | (integer) | (bytes array) | (integer) | (bytes array) |                             |


3. Error:

