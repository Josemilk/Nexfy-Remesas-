with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
i = 0
while i < len(lines):
    if (i + 6 < len(lines) and 
        "                            }\n" == lines[i] and
        "                        }\n" == lines[i+1] and
        "                    }\n" == lines[i+2] and
        "                }\n" == lines[i+3] and
        "            }\n" == lines[i+4] and
        "            Spacer(modifier = Modifier.height(12.dp))\n" == lines[i+5] and
        "        }\n" == lines[i+6]):
        
        # We only keep it if the next line (which is Text(...) has "Dashboard de inicio" somewhere inside the next few lines)
        # Actually, let's just check lines[i+8]
        if i + 8 < len(lines) and "Dashboard de inicio" in lines[i+8]:
            new_lines.extend(lines[i:i+7])
        i += 7
    else:
        new_lines.append(lines[i])
        i += 1

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.writelines(new_lines)
