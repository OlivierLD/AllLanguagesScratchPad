# Some findings and comments about OCI_CLI
###  (aka OCI_CLI for dummies)
> OCI_CLI stands for **O**racle **C**loud **I**nfrastructure **C**ommand **L**ine **I**nterface
---

- [CLI Concepts](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cliconcepts.htm)
  - The page above has child-pages worth taking a look at.
- [Install Quickstart](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm#Quickstart)

## Get started
Here is the path I took to install and run the `OCI_CLI`

### Install the OCI_CLI on your laptop
From the [Quickstart Page](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm#Quickstart), choose the installation instructions
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

Once the installation above finished successfully, from a terminal, run `oci session authenticate`.  
(make sure you've started VPN if needed) :
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
When prompted like above, I entered `35`, for `us-ashburn-1`.
```
. . .
41: us-phoenix-1, 42: us-sanjose-1): 35
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

> _**Important note**_:  
> If you are working from a terminal, and if you do not have access to a graphical desktop, 
> then you should copy the URL above (`https://login.us-ashburn-1.oraclecloud.com/v1/oauth2/authorize?action=...`) and open it in a browser (from another machine, obviously). 
> At some point, the browser will be re-directed to another URL like `http://localhost:8181/....` that will not work, for obvious reasons.
> Just replace `localhost` in the URL with the name or IP of the machine you did the `oci session authenticate` from, and you'll be good to go.

Then the browser should say:
```
Authorization completed! Please close this window and return to your terminal to finish the bootstrap process.
```
And the script above (in the terminal) resumes.   
If a `DEFAULT` profile already exists in your environment, you will also be 
prompted for a profile name. See below. I used `oliv-profile`, this name will be used later to connect, you need to remember it.
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
- [Obtain Tenancy Details](https://docs.oracle.com/en-us/iaas/autonomous-database-shared/doc/autonomous-database-support-ocid.html?Highlight=tenancy%20ocid)
- [Using the CLI](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliusing.htm)
- [CLI Command Reference](https://docs.cloud.oracle.com/en-us/iaas/tools/oci-cli/3.10.3/oci_cli_docs/index.html), the full list, the one to bookmark!

Get tenancy OCID:
In the OCI Console, User-Profile > Tenancy: `devdigital`, then click 'Show' or 'Copy' on the page
```
$ export T_OCID=ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda
```
Finding OCID of a compartment: <https://docs.oracle.com/en-us/iaas/Content/GSG/Tasks/contactingsupport_topic-Finding_the_OCID_of_a_Compartment.htm>
```
$ export C_OCID=ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda
```
Then, to get the compartment list:
```
$ oci iam compartment list -c $T_OCID --config-file ~/.oci/config --profile oliv-profile --auth security_token
{
  "data": [
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Stores accessible tag namespace with read permission",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaalxukmlrsni2pjhx7wpfp7eeyhik6afsguama6yodqjob45een5sa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "Accessible-Tag-Namespace-Read",
      "time-created": "2019-06-10T20:07:19.377000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Stores accessible tag namespaces with manage permission",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaafjydfl6jqfuo2jakyzo24rdtjfgwmaohpuw5xyw7e5qctmcblr3q",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "Accessible-Tag-Namespaces-Manage",
      "time-created": "2019-06-10T20:07:18.754000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "kjan",
          "CreatedOn": "2022-05-15T07:08:12.474Z"
        }
      },
      "description": "For Service Administration",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaagi56eogxdifrst45rmu23jjyfhjphx745ih5bnsporgl56my7qwa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "admin",
      "time-created": "2022-05-15T07:08:12.543000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "wbao",
          "CreatedOn": "2022-05-22T02:46:44.159Z"
        }
      },
      "description": "resource for build integration pipeline",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaax47hao4r6yfhcqx52bsp6kqic43c5ujkxtyaabjsfqgyfp56v77a",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "build_pipeline",
      "time-created": "2022-05-22T02:46:44.243000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "A logical compartment for all d0 DA instances",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaasj5mif3p3xxybuefngntrhdfrwo7l6xmfyrlplmm2yte7jkt6kuq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "d0-oda-instances",
      "time-created": "2019-06-05T19:35:07.638000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Compartment for resources used by deep-learning development",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa75cnygoy2aqpghii7ng6a3ljdmlayniw6zfd35rao6ecwe5266ga",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "deep-learning",
      "time-created": "2019-04-15T20:56:58.664000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "wbao",
          "CreatedOn": "2022-04-09T09:24:55.323Z"
        }
      },
      "description": "capture the data for dev pipeline history",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaz4tinm6ahnpz65rbu3cdar5rq7uiql4plpedmlhsbhixzqyi6xtq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "dev-efficiency",
      "time-created": "2022-04-09T09:24:55.633000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "compartment for leg2oci",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaayk7xovc7yulo5o7hczghvawhlm2q5lqhnpnorkv25kijgpy254mq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "devdig1",
      "time-created": "2019-01-11T21:11:29.894000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Compartment for individual development compute instances",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaupxe3ojlce5hhpnjlnramxotkiruaiyukvshroz5rphqv3a4byua",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "development",
      "time-created": "2019-03-28T00:44:15.952000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "vpurang",
          "CreatedOn": "2021-01-06T18:43:54.748Z"
        }
      },
      "description": "A compartment for storing docai related artifacts",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa6atna75swoaqtldd4yzuzy5oqnf2n77maxyvhx4zfiy7dh7mux6a",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "docai",
      "time-created": "2021-01-06T18:43:55.575000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "shepherd",
          "CreatedOn": "2020-06-05T19:51:03.568Z"
        },
        "OracleInternalReserved": {
          "ServiceType": "Other"
        }
      },
      "description": "A logical compartment for hosting Fn resources",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa4ugh7nbqpidea4bnxvguhoqsqmz5f7hpxsx75d2wok5znq434k5a",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "fn",
      "time-created": "2020-06-05T19:51:05.479000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Stores inaccessible tag namespace",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa3orpzarjaufuvjtgel6q4ubzxg5fm6obq7mhl5tmlgx7lnubnhvq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "Inaccessible-Tag-Namespace",
      "time-created": "2019-06-10T20:07:18.423000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "jtdoble",
          "CreatedOn": "2022-03-16T10:35:15.528Z"
        }
      },
      "description": "Compartment for Integration Test resources",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaat54624kco6a4wpn3zdd6zbggo2sb3cl5bq5dq2u6v6amr7j6dmqq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "IntegrationTest",
      "time-created": "2022-03-16T10:35:15.582000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "idcs-2e9d41abd92941ca9614ae8b9194bc12|21975880|Oracle America, Inc. - Internal",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaw54ubkomggjxm3kjh2dhn3oy6c7timhy7pyrnese4kibgkjwaqrq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "ManagedCompartmentForPaaS",
      "time-created": "2018-12-06T23:04:23.648000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Network Compartment",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaak6ve3ygl4haydwks72d5qkg3lodibt7pjwjmh5nw4vscye55vvkq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "Networks",
      "time-created": "2018-12-17T23:53:36.350000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "weifeng.bao@oracle.com",
          "CreatedOn": "2022-03-18T04:20:53.711Z"
        }
      },
      "description": "docker registry compartment",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaab5qyjk4ltsunju6euwat4h2iuqyc4aqpt4ybecjkdkanvybecfma",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "ocir",
      "time-created": "2022-03-18T04:20:53.793000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "A logical compartment for all DA instances",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaapsmagbn5jz2u2y2b7ixfcm536iannrl6kiyne4i6iv422rleyjda",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "oda-instances",
      "time-created": "2019-04-26T06:30:45.662000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Holds ODA instances created in the p0 environment",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaatwxygacajyakg7a75l4vi2lstzf6d63ble4unjkfvoy22on2rhxa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "p0-oda-instances",
      "time-created": "2019-07-23T21:26:47.724000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Compartment for service Image Distributor",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaavpff7o2mtnh77iu3awiq3j5gzm3ndsoytdgawxgtnll4ivld7dmq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PDSImages",
      "time-created": "2019-04-09T19:18:19.071000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "pdsociaccessmanageruat",
          "CreatedOn": "2021-03-07T22:11:03.476Z"
        }
      },
      "description": "Compartment for service Image Distributor",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaavtah2gxy7izl3en4sf75rvugttkdgkohgom2f34yjqepf44j4njq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PDSImagesUAT",
      "time-created": "2021-03-07T22:11:04.098000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT1ID compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa6cbnttmbjvoam5lfcc6i7oqyyb4rp64dqua7jnc6sbqemlpvz65a",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT1IDSessionCompartment",
      "time-created": "2019-09-17T19:35:26.643000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT1ID dependents compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaamuwpb4miqbanu2mo4mgsi3jp5v4pqomv3gyfcbzduqyjhjl4jrkq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT1IDSessionCompartmentDependent",
      "time-created": "2019-09-17T19:35:37.444000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT2ID compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaajmkeatjcg4modi2ylkvmue4ggjaftnvuftdc5mosmmrr6t3a4vwa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT2IDSessionCompartment",
      "time-created": "2019-09-23T19:57:37.777000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT2ID dependents compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaakby27vjrkuefslr2utpqxum35kqy6qk6vs6txsult25mgki24eoq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT2IDSessionCompartmentDependent",
      "time-created": "2019-09-23T19:57:48.726000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT3ID compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaatzm6rdsq4k45mryfzvibivmlgci4hc5bt62r6ajhw34dniktd56q",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT3IDSessionCompartment",
      "time-created": "2019-09-26T08:48:29.250000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT3ID dependents compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaavytz654cwfcuevhejuq3iindd6yx2rzx3tdopr7ldhhyow7id2uq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT3IDSessionCompartmentDependent",
      "time-created": "2019-09-26T08:48:40.910000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT4ID compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaayqo5vhlkyyjhtvzijd7ki47xdfah5qkwgmixl5ws3zs4w7zypbza",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT4IDSessionCompartment",
      "time-created": "2019-09-26T08:50:27.078000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT4ID dependents compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaafkfhk7vrjfmrrhdwnod3u2ciklykk5n6snz2gmc3uqsctardeifa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT4IDSessionCompartmentDependent",
      "time-created": "2019-09-26T08:50:38.887000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT5ID compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaarnr54qcs6a6uf4w6wbnwjhkt425z5gl2ham4yye3bs45tkyz7v4a",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT5IDSessionCompartment",
      "time-created": "2019-09-26T08:57:48.009000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PfBT5ID dependents compartment description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaau3wwg77geg5epnvasuo72d2zfiataytltrnt5k6xcmhbwcuclhkq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PfBT5IDSessionCompartmentDependent",
      "time-created": "2019-09-26T08:57:59.995000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PlatformBarTestCompartment Description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaax6srw5ts6ns65m3pm2nd6gyrohaufpf5rxmmopjfs24k2ubsvcqq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PlatformBarTestCompartment",
      "time-created": "2019-06-10T20:07:10.428000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PlatformBarTestDependentResourcesCompartment Description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaatnms4cqkdggcsqfelpxilosqcwzjhh24bs5nalhbr65fzi3jjw3q",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PlatformBarTestDependentResourcesCompartment",
      "time-created": "2019-06-10T20:07:39.668000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "PlatformBarTestNewCompartment Description",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa356mtmy7tl7esakr7pndtdh4x2f5ippktzuamhjvaqh3ijbjhfmq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "PlatformBarTestNewCompartment",
      "time-created": "2019-06-18T20:47:32.795000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "Pool Management For PSM-managed Service Instances",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaf5jbkz6ajbmyymucltbl7o2u4a34hkmlkpeeqly3ixjxedgc72sa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "pool-mgmt",
      "time-created": "2019-08-22T03:35:49.047000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "A logical compartment for all d1 DA instances",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaazx4ha7avnedp4svovtnxck6d6dr56k3uxrxbcku5mop64yevdskq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "s0-oda-instances",
      "time-created": "2019-06-05T19:35:07.575000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "For resources to be shared among ODA developers",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaarhtqamywjvulp5ceirrjxtah72xpusunrwko272v5q2ouzrwmsrq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "shared-resources",
      "time-created": "2019-04-08T20:47:50.368000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "wbao",
          "CreatedOn": "2022-04-09T09:44:18.428Z"
        }
      },
      "description": "situation_facade",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaz7gp2ha5pxywz2me324lnqs7wmnfnxqfgzeznsqfjjoyr3qraxdq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "situation_facade",
      "time-created": "2022-04-09T09:44:18.802000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {},
      "description": "A logical compartment for all t0 DA instances",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaeefrnc7klu6ljrzcctyezryfjjzzoywasab4eawoh3kkaq7ceyla",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "t0-oda-instances",
      "time-created": "2019-06-05T19:35:07.441000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "OracleInternalReserved": {
          "ServiceType": "Other"
        }
      },
      "description": "Test required tags",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaawb7rzzdqubpmdgm7lazoyls3l7wjr4jehpbejvcag53uxdohlstq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "TagTest",
      "time-created": "2019-10-29T11:05:40.203000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "jtdoble",
          "CreatedOn": "2021-03-18T14:58:03.308Z"
        }
      },
      "description": "test",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaa2x6nekoisj4rtf75cody6f3oulttcvdo2gamkfousnc3faohmpqq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "test1",
      "time-created": "2021-03-18T14:58:04.074000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "jtdoble",
          "CreatedOn": "2021-03-18T14:58:19.468Z"
        }
      },
      "description": "test2",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaay3vvv2zv2oqsy6veh7cydq434t6ciq7iwp7n43er6zwbix2wvkjq",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "test2",
      "time-created": "2021-03-18T14:58:20.025000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "nhauge",
          "CreatedOn": "2022-02-06T20:57:12.996Z"
        }
      },
      "description": "compartment created by terraform to run create steps of the test",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaan2uunlxpcin6re2a4fgstehk5kiduvsqj4zi2u2aqovj5ifxtmla",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "tf-test-compartment-for-create",
      "time-created": "2022-02-06T20:57:13.843000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "nhauge",
          "CreatedOn": "2022-02-06T20:57:12.898Z"
        }
      },
      "description": "compartment created by terraform to run move compartment step of the test",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaykhseeapzxnve72lsvvodddrskzady624c3hxu6tyzuwle42yisa",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "tf-test-compartment-for-move_compartment",
      "time-created": "2022-02-06T20:57:14.345000+00:00"
    },
    {
      "compartment-id": "ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda",
      "defined-tags": {
        "Oracle-Tags": {
          "CreatedBy": "nhauge",
          "CreatedOn": "2022-02-06T20:57:16.592Z"
        }
      },
      "description": "compartment created by terraform to persist static resource required by the test",
      "freeform-tags": {},
      "id": "ocid1.compartment.oc1..aaaaaaaaouozmubsim7kamvxmdfbzpn3z7rfy32ldi4x4qqnwhrzrbpea6ja",
      "inactive-status": null,
      "is-accessible": null,
      "lifecycle-state": "ACTIVE",
      "name": "tf-test-compartment-for-static_resources",
      "time-created": "2022-02-06T20:57:16.624000+00:00"
    }
  ]
}
$
```

Sample (bash) script (`os`: Object Storage, `iam`: Identity and Access Management):
```bash
#!/bin/bash
oci os ns get --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq
export T_OCID=ocid1.tenancy.oc1..aaaaaaaa46rfbmuj2cciouoyz6sbmsbxkzjb3ibkfvhd5vttptgcpjruskda
oci iam region list --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq
oci iam compartment list -c $T_OCID --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq
#
echo -e "Done!"
```

- [jq cheat sheet](https://lzone.de/cheat-sheet/jq).

Just the region names:
```
$ oci iam region list --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | { name }'
$ oci iam region list --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | { name } | join("") '
```
Just compartment names and ids:
```
$ oci iam compartment list -c $T_OCID --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | {name, "compartment-id"} '
```
or 
```
$ oci iam compartment list -c $T_OCID --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | {name, "compartment-id"} | join(" : ")'
```
with a filter on compartment name:
```
$ oci iam compartment list -c $T_OCID --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | select(.name | contains("dev")) | { name, "compartment-id" } | join(" : ")'
```
User list
```
$  oci iam user list --config-file ~/.oci/config --profile oliv-profile --auth security_token --all | jq ' .data[] | { name } | join("")'
```
Various queries:
```
$ oci iam policy list --compartment-id $T_OCID --config-file ~/.oci/config --profile oliv-profile --auth security_token
$ oci os bucket list -c $T_OCID --namespace devdigital --config-file ~/.oci/config --profile oliv-profile --auth security_token
$ oci os bucket list --config-file ~/.oci/config --profile oliv-profile --auth security_token -c $T_OCID
$ oci os bucket list -c $T_OCID --profile oliv-profile --namespace devdigital --auth security_token
```
Formatted bucket (Object Storage) list:
```
$ oci os bucket list -c $T_OCID --namespace devdigital --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | { name, namespace } | join(", bucket in ") '
"oliv-bucket-20201222-0748, bucket in devdigital"
```
Bucket content:
```
BUCKET_NAME=oliv-bucket-20201222-0748
$ oci os object list -ns devdigital -bn ${BUCKET_NAME} --config-file ~/.oci/config --profile oliv-profile --auth security_token
{
  "data": [
    {
      "archival-state": null,
      "etag": "971a7b3f-323a-47a4-ba2e-44ae66f68099",
      "md5": "1B2M2Y8AsgTpgAmY7PhCfg==",
      "name": "SampleApplication-01/",
      "size": 0,
      "storage-tier": "Standard",
      "time-created": "2020-12-23T15:35:01.661000+00:00",
      "time-modified": "2020-12-23T15:35:01.668000+00:00"
    }
  ],
  "prefixes": []
}
```
Notice the size ;)

A non-empty bucket:
```
$ oci os bucket list -c ocid1.compartment.oc1..aaaaaaaaupxe3ojlce5hhpnjlnramxotkiruaiyukvshroz5rphqv3a4byua --namespace devdigital --config-file ~/.oci/config --profile oliv-profile --auth security_token | jq '.data[] | { name, namespace } | join(", bucket in ") '
```

```
$ oci os object list -ns devdigital -bn olediour-bucket-20201215-1103 --config-file ~/.oci/config --profile oliv-profile --auth security_token
{
  "data": [
    {
      "archival-state": null,
      "etag": "cb3c40d7-85c9-44c6-ad1b-8179c1ec459c",
      "md5": "1B2M2Y8AsgTpgAmY7PhCfg==",
      "name": "SampleApplication/",
      "size": 0,
      "storage-tier": "Standard",
      "time-created": "2020-12-15T19:06:42.678000+00:00",
      "time-modified": "2020-12-15T19:06:42.685000+00:00"
    },
    {
      "archival-state": null,
      "etag": "f774bfaa-a61b-4b8c-835b-6427048689b2",
      "md5": "Xj8DsSKYJzFeYbnghJ7u1w==",
      "name": "SampleApplication/img_gas.receipt.jpg",
      "size": 286834,
      "storage-tier": "Standard",
      "time-created": "2020-12-15T19:08:14.237000+00:00",
      "time-modified": "2020-12-15T19:08:14.505000+00:00"
    }
  ],
  "prefixes": []
}
```
If you have access to the `development` namespace (aka compartment):
```
$ oci os object list -ns development -bn olivierlediouris-docai-test-bucket --config-file ~/.oci/config --profile oliv-profile --auth security_token
```

Download data from ObjectStorage. 
- Tenancy `devdigital`
- Bucket `olediour-bucket-20201215-1103`
- Object `SampleApplication/img_gas.receipt.jpg`
- Downloaded as `img.jpg`
```
$ oci os object get -ns devdigital -bn olediour-bucket-20201215-1103 --name SampleApplication/img_gas.receipt.jpg --file img.jpg --config-file ~/.oci/config --profile oliv-profile --auth security_token
Downloading object  [####################################]  100%
$ ll img.jpg 
43400261 568 -rw-r--r--  1 olediour  staff  286834 Jun 29 09:25 img.jpg
$
```

---
