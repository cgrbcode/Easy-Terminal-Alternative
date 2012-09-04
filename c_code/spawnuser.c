#include <unistd.h>
#include <sys/ioctl.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include  <sys/types.h>
int startUserService(const char* user,const char* password,const char* command);
int startUserService(const char* user,const char* password,const char* command) {
	pid_t childPid;
	int fd[2];
	int fdm,fds;
	char *slavename;
	extern char *ptsname();
	char reading[50];
	pipe(fd);
	fdm=open("/dev/ptmx",O_RDWR);
	grantpt(fdm);
	unlockpt(fdm);
	slavename=ptsname(fdm);
	fds = open(slavename,O_RDWR);
	childPid=fork();
    if(childPid<0){
        printf("id of %d failed to fork\n",childPid);
    }
	if(childPid==0)
	{
		close(fd[0]);
		close(fdm);
		close(0);//close stdin
		dup(fds);//send stdin to fds
		close(1);//close stdout
		dup(fds);//send stdout to fds
		close(2);
		dup(fds);
		close(fds);
		setvbuf(stdout,(char*)NULL,_IONBF,0);
		setvbuf(stderr,(char*)NULL,_IONBF,0);
		setsid();
		ioctl(0, TIOCSCTTY, 1);
		execlp("su", "su", user, (char *)0);
		return 1;
	} else {
		close(fd[1]);
		close(fds);
		char reading[2];
		int readN;
		FILE *f = fdopen( fdm, "r+" );
		char line[200];
		setvbuf(f,(char*)0,_IONBF,0);
		if(fgets(reading,2,f)) {
			strcat(line,reading);
			fprintf(f,"%s\n",password);
			fprintf(f,"whoami\n");
		}
        char sent_command = 0;
		while(fgets(reading,2,f)>0) {
			if(reading[0]=='\r'||reading[0]=='\n') {
				int comp = strcmp(line,user);
                printf("%s\n",line);
                if(sent_command>0){
                    sent_command++;
                }
				if(comp==0){
				 fprintf(f,"%s & \n",command);
                 fflush(f);
			     sent_command=1;
				 //fprintf(f,"exit\n");
  		         //sleep(1);
				 //close(fdm);
				 //fclose(f);
                 //return 1;
				}else if(sent_command>=4&&strlen(line)>5){
                    printf("exiting\n");
                    sleep(2);
                    fprintf(f,"exit\n");
                    sleep(1);
                    close(fdm);
                    fclose(f);
                    return 1;
                }
				line[0]='\0';
            } else {
				strcat(line,reading);
			}
		}
		return 2;
	}
	return 0;
}
int main(int argc, const char *args[]){
    char password[50];
    char command[200];
    fprintf(stdout,"password:\r\n");
    fflush(stdout);
    fgets(password,50,stdin);
    int i = strlen(password)-1;
    if( password[i] == '\n') 
        password[i] = '\0';

    for(i=2;i<argc;i++){
        strcat(command,args[i]);
        strcat(command," ");
    }
    printf("starting service: %s \r\n",command);
    fflush(stdout);
    int ret = startUserService(args[1],password,command);
    printf("connected\r\n");
  return 1;
}


