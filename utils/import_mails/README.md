

# Import_mails module

Python script aiming at improving the mail export process on LinShare.

### Table of contents
  *   [Prerequisites](#prerequis)
  *   [Quick start](#config_module)
  *   [Generated files](#generated_files)

## <a name="prerequis"></a> Prerequisites

Before executing this script, you need :
*  Access to the LinShare PostgreSQL
* Python 3
*  pip3

	```
      sudo apt-get install python3-pip
	```
  *  psycopg2-binary to be able to make a connection with your database
  
        ```
        pip3 install psycopg2-binary
        ```
    
##   <a name="config_module"></a> Quick start

Now the Python environment is set, we can set up the module.
Go to LINSHARE_WORKSPACE/utils/import_mails.
Run the following command to launch the script :

```
$ python3 app.py
```

If you get at least one error, like displayed below, you need to edit the file config.json.

	
	        Name             State     
	------------------------------
	Config Fields            OK
	Get Mail Content Types   ERROR
	Linshare Database        ERROR
	------------------------------
	
	
	
### Editing the setup file
Create a file config.son in LINSHARE_WORKSPACE/utils/import_mails.
* Edit the config.json file with only the parameters which differ from the default configuration to match your LinShare setup.

	```json
	{
		"dbname": "linshare",
		"user": "DATABASE_USER",
		"host": "LINSHARE_HOST",
		"port": "DATABASE_PORT",
		"password": "DATABASE_PASSWORD",
		"path_mail_content_types": "../../src/main/java/org/linagora/linshare/core/domain/constants/MailContentType.java"
	}
	```
*NB : `path_mail_content_types` indicates the path of the enum file of all the mail in LinShare.*

By running the following command,
 
	 $ python3 app.py

you should now have this result, and the running script :

	
	-----Check configuration------
	        Name             State     
	------------------------------
	Config Fields            OK
	Get Mail Content Types   OK
	Linshare Database        OK
	------------------------------
	
	
##  <a name="generated_files"></a> Generated files

Now we got successful execution, export files has been generated in the folder `src/main/resources/sql/common` :
*  `import-mail.sql` : file with the structure and all the update : 
	* `import_mail_structure.sql` + `import_mail_update.sql`
*  `import_mail_structure.sql` : file inserting the the mail structures in the database
* `mail_updates` folder with :
	*  the folder `mail_updates` containing the UPDATE files of the mail contents
	*  `import_mail_update.sql` : file of all updates from the previous folder

	
	...
	|-- import_mail_structure.sql
	|-- import-mail.sql
	|-- mail_updates
		|-- mail_content
			|-- ...
		|-- mail_footer
			|-- ...
		|-- mail_layout
			|-- ...
		|-- import_mail_update.sql
	
