**Setting up Java kernel for Jupyter in** [`ondemand.hpc.icm.edu.pl`](ondemand.hpc.icm.edu.pl)

- on *topola* (`ssh login@hpc.icm.edu.pl`) create directory `~/ondemand/dev/`
- go to website [`ondemand.hpc.icm.edu.pl`](ondemand.hpc.icm.edu.pl) and select  *Develop* &#8594; *My Sandbox Apps (Development)* on top navigation bar
- choose *New App* &#8594; *Clone Existing App* and fill the form:
	- Directory name: jupyter-java
	- Git remote: [`https://github.com/OSC/bc_example_jupyter.git`](https://github.com/OSC/bc_example_jupyter.git)
	- Submit
- copy `jup.tar.xz` to `~/ondemand/dev/jupyter-java/` directory (e.g. *Files* &#8594; *topola home* on top navigation bar). `jup.tar.xz` contains config files for ondemand app
- back in CLI on *topola* change working directory to  `~/ondemand/dev/jupyter-java/` and run `tar xJfv jup.tar.xz` to extract files from the tarball


- installing Java kernel for Jupyter (IJava) ([https://github.com/SpencerPark/IJava](https://github.com/SpencerPark/IJava)):
	- on *topola* go back to home directory `cd ~`
	- `srun -N 1 -n 1 --partition=topola --account=GS80-31 --qos=hpc --time=01:00:00 --pty /bin/bash -l`
	- `export PATH=/apps/java/openjdk-11.0.9_11/bin:$PATH`
	- `wget https://github.com/SpencerPark/IJava/releases/download/v1.3.0/ijava-1.3.0.zip`
	-  `mkdir ijava`
	- `unzip ijava-1.3.0.zip -d ijava`
	- `cd ijava`
	- `module load /apps/common/conda/2020.07/modulefile`
	- `python3 install.py --user`

--

- in home directory clone pdyn2-stack: `git clone git@git.icm.edu.pl:fdreger/pdyn2-stack.git `
- change access permisions to `pdyn2-stack` directory by running `chmod -R 700 pdyn2-stack/`. We need this step, because we use secret key to access s3 by DVC.

--

Now follow [README](README.md) from main directory

--


To run our app go to [ondemand.hpc.icm.edu.pl](ondemand.hpc.icm.edu.pl) &#8594; *Develop* &#8594; *My Sandbox Apps (Development)* &#8594; *Launch Jupyter Notebook*
