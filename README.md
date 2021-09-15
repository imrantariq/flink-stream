

#### This project is taken from another Github repository

https://github.com/afedulov/fraud-detection-demo


This is customised version of that project where its setup to run locally and can process multiple rules and transactions


Rules
-----
Can be sent to stream via netcat

nc -lk 9991


Sample Rule
{ "ruleType": "default", "ruleId": 1, "ruleState": "ACTIVE", "groupingKeyNames": ["paymentType"], "unique": [], "aggregateFieldName": "paymentAmount", "aggregatorFunctionType": "SUM","limitOperatorType": "GREATER","limit": 50, "windowMinutes": 2}


Examples of Rule Control Commands to delete Rule etc:
{"ruleState": "CONTROL", "controlType":"DELETE_RULES_ALL"}
{"ruleState": "CONTROL", "controlType":"EXPORT_RULES_CURRENT"}
{"ruleState": "CONTROL", "controlType":"CLEAR_STATE_ALL"}


Transaction/Event
-----------------
Can be sent to stream via netcat

nc -lk 9992


Sample Transaction/Event

{"ruleType": "default","transactionId": 1,"eventTime": 1631261698733,"payeeId": 2,"beneficiaryId": 2,"paymentAmount": 22,"paymentType": "CSH"}


Sample Output alert
{"ruleId":2,"violatedRule":{"ruleId":2,"ruleState":"ACTIVE","groupingKeyNames":["paymentType"],"unique":[],"aggregateFieldName":"paymentAmount","aggregatorFunctionType":"SUM","limitOperatorType":"LESS_EQUAL","limit":500,"windowMinutes":2,"controlType":null,"windowMillis":120000},"key":"{paymentType=CSH}","triggeringEvent":{"transactionId":2,"eventTime":1,"payeeId":2,"beneficiaryId":2,"paymentAmount":332,"paymentType":"CSH","ingestionTimestamp":1631192911550},"triggeringValue":332}

========
HackRule (RuleType2)
----
{ "ruleType": "hackrule", "ruleId": 11, "ruleState": "ACTIVE", "groupingKeyNames": ["email"], "aggregateFieldName": "attempt", "aggregatorFunctionType": "SUM","limitOperatorType": "GREATER","limit": 2, "windowMinutes": 2}


Transaction
{ "ruleType": "hackrule", "transactionId": 1, "eventTime": 1631261698733,"email": "test@gmail.com","attempt": 2}
{ "ruleType": "hackrule", "transactionId": 1, "eventTime": 163126169,"email": "abc@gmail.com","attempt": 2}


