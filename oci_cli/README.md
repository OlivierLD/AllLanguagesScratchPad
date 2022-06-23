# Some findings and comments about OCI_CLI
> OCI_CLI stands for Oracle Cloud Infrastructure Command Line Interface
---

- [CLI Concepts](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cliconcepts.htm)
  - The page above has child-pages worth taking a look at.
- [Install Quickstart](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm#Quickstart)

## Get started
Here is the path I took to install and run the `OCI_CLI`

### Install the OCI_CLI on your laptop
From the [Quickstart Page](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm#Quickstart), choose the installer
that fits the OS of your laptop.  
From a Mac, I did a:
```
$ brew update && brew install oci-cli
$ oci --version
3.10.3
$
```
> _Be aware_:  `brew install oci-cli` can take (a lot of) time...

### Configure
The configuration step will create some file on your laptop (like `~/.oci/config`),
containing the instance(s) you want to target, along with the related credentials.

I found many documents about that, many of them were obsolete, some were wrong, others contradicting each other,
here is a path that worked for me.

Once the installation above finished successfully, make sure you've started VPN if needed, and from a terminal, run the following:
```
$ oci session authenticate
Enter a region by index or name(e.g.
1: af-johannesburg-1, 2: ap-chiyoda-1, 3: ap-chuncheon-1, 4: ap-dcc-canberra-1, 5: ap-hyderabad-1,
6: ap-ibaraki-1, 7: ap-melbourne-1, 8: ap-mumbai-1, 9: ap-osaka-1, 10: ap-seoul-1,
11: ap-singapore-1, 12: ap-sydney-1, 13: ap-tokyo-1, 14: ca-montreal-1, 15: ca-toronto-1,
16: eu-amsterdam-1, 17: eu-frankfurt-1, 18: eu-marseille-1, 19: eu-milan-1, 20: eu-paris-1,
21: eu-stockholm-1, 22: eu-zurich-1, 23: il-jerusalem-1, 24: me-abudhabi-1, 25: me-dcc-muscat-1,
26: me-dubai-1, 27: me-jeddah-1, 28: sa-santiago-1, 29: sa-saopaulo-1, 30: sa-vinhedo-1,
31: uk-cardiff-1, 32: uk-gov-cardiff-1, 33: uk-gov-london-1, 34: uk-london-1, 35: us-ashburn-1,
36: us-gov-ashburn-1, 37: us-gov-chicago-1, 38: us-gov-phoenix-1, 39: us-langley-1, 40: us-luke-1,
41: us-phoenix-1, 42: us-sanjose-1):
```
When prompted like above, I entered `35`.
```
. . .
us-phoenix-1, 42: us-sanjose-1): 35
    Please switch to newly opened browser window to log in!
    You can also open the following URL in a web browser window to continue:
https://login.us-ashburn-1.oraclecloud.com/v1/oauth2/authorize?action=login&client_id=iaas_console&response_type=token+id_token&nonce=62f8f5ee-1777-48a3-a5b4-def3337c7730&scope=openid&public_key=eyJrdHkiOiAiUlNBIiwgIm4iOiAieWFuZUg1MjltMU1jdHdZcXlmUnNSVW1HeTJRZndhUUVSS3lSNmhaT2hzeC1aLWZwbHk0LWVWSnlqMi02bDZuaFg3NGhtYU5wdUdNWlVLWG5UX2gtR3lMMzFQV2czbGhmV1ljTjh2ZGpJQkdKMVpiVnhBZTVOM1VJZERmUXV4YUlqMmxYajAxTlB6RG53SW5mOUwzTVRwZHlCQUFtenVDRDhPRDNReUpoLXFOa3lrTHo5NUw3SEJPdGIycUFRQ21mQVdOVTF1b1RQalZscjF3NmdyUkF2ckZuaHNCeTVvRkFmYnN0WElrcHFUVjR4UGU1dTBYZ1A3cWh4UWlpWUFjbnhLMUdYRDBfVDg1dW1fd0E1MXYzdW1lTnNSbGtTVmZmMmptVFpxRk02UXVkcURjeFk2UEtXTTkwRXhlOHJnbjRWSUpmVmc2X1ZXUnVWZWdYTkMwVjd3IiwgImUiOiAiQVFBQiIsICJraWQiOiAiSWdub3JlZCJ9&redirect_uri=http%3A%2F%2Flocalhost%3A8181
```
The last step above will open your default browser, and prompt you for an OCI connection.

I used
- tenancy: `devdigital`
- Then choose the Direct sign-in option
- user-name: `oda-dev`
- password: As explained [here](https://confluence.oraclecorp.com/confluence/display/IBS/OCI+Compute+Instance+for+Digital+Assistant+Development), you should have access to this page.

Then the browser should say:
```
Authorization completed! Please close this window and return to your terminal to finish the bootstrap process.
```
And the script above resumes.
You will also be prompted for a profile name. See below. I used `oliv-profile`, this name will be used later to connect,
you need to remember it.

```
    Completed browser authentication process!
Enter the name of the profile you would like to create: oliv-profile
Config written to: /Users/olediour/.oci/config

    Try out your newly created session credentials with the following example command:

    oci iam region list --config-file /Users/olediour/.oci/config --profile oliv-profile --auth security_token

$
```
If this is what you see, you should be good with your config.  
If you are curious, you might want to take a look at the generated config file:
```
$ cat ~/.oci/config
```
You will find in there the definition of the profile(s) already created.

### And run!
As mentioned above, you can now run OCI_CLI commands.  
Let's run a `region list` command, as suggested.  
Notice the `oci aim`, the command, the reference to the `--config-file`, the reference to the `--profile`, etc.
```
$ oci iam region list --config-file /Users/olediour/.oci/config --profile oliv-profile --auth security_token
{
  "data": [
    {
      "key": "AMS",
      "name": "eu-amsterdam-1"
    },
    {
      "key": "ARN",
      "name": "eu-stockholm-1"
    },
    {
      "key": "AUH",
      "name": "me-abudhabi-1"
    },
    {
      "key": "BOM",
      "name": "ap-mumbai-1"
    },
    {
      "key": "CDG",
      "name": "eu-paris-1"
    },
    {
      "key": "CWL",
      "name": "uk-cardiff-1"
    },
    {
      "key": "DXB",
      "name": "me-dubai-1"
    },
    {
      "key": "FRA",
      "name": "eu-frankfurt-1"
    },
    {
      "key": "GRU",
      "name": "sa-saopaulo-1"
    },
    {
      "key": "HYD",
      "name": "ap-hyderabad-1"
    },
    {
      "key": "IAD",
      "name": "us-ashburn-1"
    },
    {
      "key": "ICN",
      "name": "ap-seoul-1"
    },
    {
      "key": "JED",
      "name": "me-jeddah-1"
    },
    {
      "key": "JNB",
      "name": "af-johannesburg-1"
    },
    {
      "key": "KIX",
      "name": "ap-osaka-1"
    },
    {
      "key": "LHR",
      "name": "uk-london-1"
    },
    {
      "key": "LIN",
      "name": "eu-milan-1"
    },
    {
      "key": "MAD",
      "name": "eu-madrid-1"
    },
    {
      "key": "MEL",
      "name": "ap-melbourne-1"
    },
    {
      "key": "MRS",
      "name": "eu-marseille-1"
    },
    {
      "key": "MTZ",
      "name": "il-jerusalem-1"
    },
    {
      "key": "NRT",
      "name": "ap-tokyo-1"
    },
    {
      "key": "PHX",
      "name": "us-phoenix-1"
    },
    {
      "key": "QRO",
      "name": "mx-queretaro-1"
    },
    {
      "key": "SCL",
      "name": "sa-santiago-1"
    },
    {
      "key": "SIN",
      "name": "ap-singapore-1"
    },
    {
      "key": "SJC",
      "name": "us-sanjose-1"
    },
    {
      "key": "SYD",
      "name": "ap-sydney-1"
    },
    {
      "key": "VCP",
      "name": "sa-vinhedo-1"
    },
    {
      "key": "YNY",
      "name": "ap-chuncheon-1"
    },
    {
      "key": "YUL",
      "name": "ca-montreal-1"
    },
    {
      "key": "YYZ",
      "name": "ca-toronto-1"
    },
    {
      "key": "ZRH",
      "name": "eu-zurich-1"
    }
  ]
}
$
```
### Good to know
After a while, your session will expire. `oci_cli` will prompt you re-authenticate:
```
ERROR: This CLI session has expired, so it cannot currently be used to run commands
Do you want to re-authenticate your CLI session profile? [Y/n]: y
```
And you will go through the authenticate steps just like before.

> See this [good doc](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/clitoken.htm) on Token Based Authentication. 

In case of wierd messages, you might want to check out the notes at <https://github.com/OlivierLD/oci-java-sdk/blob/master/oliv-notes.md#bulk-notes-oci--co>...

### Next
- [Using tyhe CLI](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliusing.htm)

---
