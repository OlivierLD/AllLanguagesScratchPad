MACHINE:str = "100.111.136.104"
OPTION:str = "generic"

rest_url:str = f"http://{MACHINE}/voice/recognize/en-us/{OPTION}"

print(rest_url)
