#include <unistd.h>
#include <sys/ioctl.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include "cgrb_eta_server_Login.h"
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>

JNIEXPORT int JNICALL Java_cgrb_eta_server_Login_login(JNIEnv *env, jobject this,jstring userj,jstring passwordj) {
	const char *user = (*env)->GetStringUTFChars(env,userj, 0);
	const char *password = (*env)->GetStringUTFChars(env,passwordj, 0);

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
		(*env)->ReleaseStringUTFChars(env, passwordj, password);
		(*env)->ReleaseStringUTFChars(env, userj, user);
		return 0;
	} else {
		close(fd[1]);
		close(fds);
		char reading[2];
		int readN;
		FILE *f = fdopen( fdm, "r+" );
		char line[100];
		setvbuf(f,(char*)0,_IONBF,0);
		if(fgets(reading,2,f)) {
			strcat(line,reading);
			//send the password
			fprintf(f,"%s\n",password);
			fprintf(f,"whoami\n");
			fprintf(f,"exit\n");
		}
		while(fgets(reading,2,f)>0) {
			// printf("%s",reading);
			if(reading[0]=='\r'||reading[0]=='\n') {
				// printf("newline =%s=\n",line);
				int comp = strcmp(line,user);
				if(comp==0) {
					(*env)->ReleaseStringUTFChars(env, passwordj, password);
					(*env)->ReleaseStringUTFChars(env, userj, user);
					return 1;
				}
				line[0]='\0';
			} else {
				strcat(line,reading);
			}
		}
		(*env)->ReleaseStringUTFChars(env, passwordj, password);
		(*env)->ReleaseStringUTFChars(env, userj, user);
		return 0;
		//sleep(1);
		//write(fdm,"Temp12\n",8);
	}
	(*env)->ReleaseStringUTFChars(env, passwordj, password);
	(*env)->ReleaseStringUTFChars(env, userj, user);
	return 0;
}

JNIEXPORT int JNICALL Java_cgrb_eta_server_Login_changePassword(JNIEnv *env, jobject this,jstring userj,jstring oldPasswordj,jstring newPasswordj) {
	const char *user = (*env)->GetStringUTFChars(env,userj, 0);
	const char *password = (*env)->GetStringUTFChars(env,oldPasswordj, 0);
	const char *newPassword = (*env)->GetStringUTFChars(env,newPasswordj, 0);

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
				if(comp==0){
				 fprintf(f,"passwd\n");
                 fflush(f);
			     sent_command=1;
                }
				line[0]='\0';
            } else if(reading[0]==':'&&sent_command>0){
                if(sent_command==1){
                    fprintf(f,"%s\n",password);
                    printf("%s\n",password);
                    fflush(f);
                }else{
                    fprintf(f,"%s\n",newPassword);
                    fflush(f);
                }
                sent_command++;
            }else {
				strcat(line,reading);
			}
		}
		return 2;
	}
	return 0;
}
JNIEXPORT int JNICALL Java_cgrb_eta_server_Login_startUserService(JNIEnv *env, jobject this,jstring userj,jstring passwordj,jstring commandj) {
        const char *user = (*env)->GetStringUTFChars(env,userj, 0);
        const char *password = (*env)->GetStringUTFChars(env,passwordj, 0);
        const char *command = (*env)->GetStringUTFChars(env,commandj, 0);
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

        setsid();

        childPid=vfork();
        if(childPid==0)
        {
                //unmask(0);
                setsid();
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
                return 0;
        } else {
                close(fd[1]);
                close(fds);
                char reading[2];
                int readN;
                FILE *f = fdopen( fdm, "r+" );
                char line[100];
                setvbuf(f,(char*)0,_IONBF,0);
                if(fgets(reading,2,f)) {
                        strcat(line,reading);
                        //send the password
                        fprintf(f,"%s\n",password);
                        fprintf(f,"whoami\n");
                }
                while(fgets(reading,2,f)>0) {
                        // printf("%s",reading);
                        if(reading[0]=='\r'||reading[0]=='\n') {
                                // printf("newline =%s=\n",line);
                                int comp = strcmp(line,user);
                                if(comp==0) {
                                        fprintf(f,"%s \n",command);
                                        sleep(1);
                                        fprintf(f,"exit\n");
                                        sleep(1);
                                        close(fdm);
                                        fclose(f);
                                        (*env)->ReleaseStringUTFChars(env, passwordj, password);
                                        (*env)->ReleaseStringUTFChars(env, userj, user);
                                        (*env)->ReleaseStringUTFChars(env, commandj, command);
                                        int status;
                                        wait(&status);
                                        free(env);
                                        return 1;
                                }

                                line[0]='\0';
                        } else {
                                strcat(line,reading);
                        }
                }
                return 0;
        }
        return 0;
}