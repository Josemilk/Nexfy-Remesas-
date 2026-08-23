import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# I will append } before `Spacer(modifier = Modifier.height(12.dp))` which is right before `Button(onClick = onNavigateToNewDelivery`
search = """            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Big Action Button "+ Nueva entrega" """
replace = """            }
            } // close else
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Big Action Button "+ Nueva entrega" """

if search in content:
    content = content.replace(search, replace)
    print("Fixed!")
else:
    print("Not found! trying regex")
    content = re.sub(r'            }\n        }\n        Spacer\(modifier = Modifier.height\(12.dp\)\)\n        // Big Action Button "\+ Nueva entrega"', 
                     '            }\n            }\n        }\n        Spacer(modifier = Modifier.height(12.dp))\n        // Big Action Button "+ Nueva entrega"', 
                     content)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
