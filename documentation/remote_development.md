# How to work remotely on topola
First, we must have bash as our default shell in topola (it is configurated in LDAP, so have to ask Xahil to change it).

Next, we should add /apps/java/openjdk-11.0.9_11/bin to our PATH (e.g. by running `echo "export PATH=/apps/java/openjdk-11.0.9_11/bin:$PATH" >> ~/.bashrc`).
We can skip this step, and configure it at the end of the process, by selecting:project structure ⟶ project settings ⟶ sdk ⟶ add SDK ⟶ jdk ⟶ choose `/apps/java/openjdk-11.0.9_11/`.

We also need to have cloned this repo somewhere inside our home directory on topola.
- install JetBrains Gateway from [here](https://www.jetbrains.com/remote-development/gateway/). The newer version the better, because there are still quite a lot of bugs.
- Now we need to get our session going, so we need to run:
    ```
    srun -N [number of nodes] -n [number of cores] --partition=topola --mem=[memory in MB] --account=GS80-31 --time=[resevation time] --pty /bin/bash -l
    so e.g.
    srun -N 1 -n 8 --partition=topola --mem=30000 --account=GS80-31 --time=3:00:00 --pty /bin/bash -l
    ```
    Message like this should pop up:

    ```
    srun: job 6323859 queued and waiting for resources
    srun: job 6323859 has been allocated resources
    ==========================================
    SLURM_JOB_ID = 6323859
    SLURM_JOB_NODELIST = t12-1
    ==========================================
    ```
    We are interested in SLURM_JOB_NODELIST, so in this case t12-1.
    
    If You don't want to have terminal window open during your work on topola, You can use _screen_ (documentation [here](https://www.gnu.org/software/screen/manual/screen.html)).  
- Now let's connect to our node. To do so we must make a tunnel:
    ```
    ssh -L 8899:[here goes SLURM_JOB_NODELIST]:22 login@hpc.icm.edu.pl
    so in our case
    ssh -L 8899:t12-1:22 login@hpc.icm.edu.pl
    ```
    It is now available under `localhost:8899`.
- Open JetBrains Gateway  ⟶ `new connection`. Fill:
  ```
  username = [your hpc username]
  host = localhost
  port = 8899
  ```
  On the next screen choose IDE version to install and your project directory on remote (pdyn2_stack location). Accept by clicking `Download and Start IDE`. It will take some time.

Done! You can adjust settings like memory for IDE etc. in `Connection details` tab on the top left.
