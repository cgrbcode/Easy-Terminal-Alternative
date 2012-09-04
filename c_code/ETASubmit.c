#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <sys/stat.h>         
#include <sys/types.h>            
#include <unistd.h>

char *rand_str( int size);
void startETAService();
int main (int argc, const char * argv[]) {
	char working_path[300];
	
    if(argc==1){
		execl("ETASubmit","",(char *) 0);
		return 0;
	}
	if(strncmp(argv[1], "-h", 2)==0){
		printf("Usage:\n");
		printf("\tETASubmit -c '<command>' -m <max_memory> -P <number_processors> -r <Run_ID> -p <priority> -M  -q <queue> -s <file>\n");
		printf("		   -c	The command to submit. (REQUIRED: Make sure to use '')\n");
		printf("		   -m	The maximum memory needed for this job (1G, 4G, 32G etc.).\n");
		printf("		   -P	The number of processors needed for this job if you have a threaded application (default 1).\n");
		printf("		   -r	The ETA job name.\n");
		printf("		   -q	The QUEUE to use. (default to use any node you have access to)\n");
		printf("		   -p	The priority of job submitted. (range -10 to 10, default 0)\n");
		printf("		   -M	Send notification on job completion.\n");
		printf("		   -W	wait for job # to finish before running this job.\n");
		printf("		   -f	assign a parent job for this job.\n");
		printf("		   -S	save standard out to this file.\n");
		printf("		   -h	Print Help Page.\n");	
		return -1;
	}
	getcwd(working_path, sizeof(working_path));
	srand(time(NULL));
	char* jobs = "/ETA/jobs/";
	char* tempETAPath="/ETA/.running";
	char* temp = rand_str(20);
	char* home = getenv("HOME");
	char* jobsPath = malloc(sizeof(char)*(strlen(jobs)+strlen(home)));
	strcat(jobsPath,home);
	strcat(jobsPath,jobs);
	
	char* etaStart = malloc(sizeof(char)*(strlen(home)+strlen(tempETAPath)));
	strcat(etaStart,home);
	strcat(etaStart,tempETAPath);					
	
	char* path = malloc(sizeof(char)*(strlen(jobsPath)+strlen(temp)));
	strcat(path,jobsPath);
	strcat(path,temp);
	
	struct stat stat_e; 
	stat (etaStart, &stat_e);
	if(!S_ISREG(stat_e.st_mode)){
		//this path doesn't exist so we should probably create it
		startETAService();
	}
	
	struct stat stat_p; 
	stat (path, &stat_p);
	int exists=S_ISREG(stat_p.st_mode);
	while(exists){
		temp = rand_str(20);
		path[strlen(jobsPath)]='\0';
		strcat(path,temp);
		struct stat stat_p2; 
		stat (path, &stat_p2);
		exists=S_ISREG(stat_p2.st_mode);
		printf("file existed new path=%s\t%i\n",path,exists);
	}
	
	const char* nativeRequest;
	const char* memory;
	const char* threads;
	const char* queue;
	const char* jobName;
	const char* waitFor;
	const char* parent;
	const char* stdOutPath;
	char  notify=0;
	const char* parentName;
	const char* command;
	const char* priority;
	
	
	int startArg = 0;
	for(int i=1;i<argc;i++){
		if(strcmp (argv[i],"-R") == 0){
			nativeRequest=argv[++i];
		}else if(strcmp (argv[i],"-m") == 0){
			memory=argv[++i];
		}else if(strcmp (argv[i],"-P") == 0){
			threads=argv[++i];
		}else if(strcmp (argv[i],"-p") == 0){
			priority=argv[++i];
		}else if(strcmp (argv[i],"-q") == 0){
			queue=argv[++i];
		}else if(strcmp (argv[i],"-r") == 0){
			jobName=argv[++i];
		}else if(strcmp (argv[i],"-W") == 0){
			waitFor=argv[++i];
		}else if(strcmp (argv[i],"-cp") == 0){
			parentName=argv[++i];
		}else if(strcmp (argv[i],"-S") == 0){
			stdOutPath=argv[++i];
		}else if(strcmp (argv[i],"-M") == 0){
			notify=1;
		}else if(strcmp (argv[i],"-f") == 0){
			parent=argv[++i];
		}else if(strcmp (argv[i],"-c") == 0){
			command=argv[++i];
		}else{
			//well it looks like the start of the command is here
			startArg=i;
			break;
		}
		startArg=i+1;
	}
	printf("creating file %s\n",path);
	
	FILE* job_file=fopen(path,"w");
	fprintf(job_file,"#working-folder\t%s\n",working_path);
	if(nativeRequest!=NULL){
		fprintf(job_file,"#native-request\t%s\n",nativeRequest);
	}
	if(threads!=NULL){
		fprintf(job_file,"#threads\t%s\n",threads);
	}
	if(queue!=NULL){
		fprintf(job_file,"#job-name\t%s\n",jobName);
	}
	if(waitFor!=NULL){
		fprintf(job_file,"#wait-for\t%s\n",waitFor);
	}
	if(memory!=NULL){
		fprintf(job_file,"#memory\t%s\n",memory);
	}
	if(parentName!=NULL){
		fprintf(job_file,"parent-name\t%s\n",parentName);
	}
	if(stdOutPath!=NULL){
		fprintf(job_file,"std-out-path\t%s\n",stdOutPath);
	}
	if(notify==1){
		fprintf(job_file,"#notify");
	}
	if(parent!=NULL){
		fprintf(job_file,"#parent\t%s\n",parent);
	}
	if(command!=NULL){
		fprintf(job_file,"#command\t%s\n",command);
	}
	if(priority!=NULL){
		fprintf(job_file,"#priority\t%s\n",priority);
	}
	
	for(int i=startArg;i<argc;i++){
		//fprintf(test, argv[i]);
		fprintf(job_file,"%s\n",argv[i]);
	}
	fclose(job_file);
    return 0;
}

char *rand_str( int size){
	static const char text[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	char* dst = malloc(sizeof(char)*size);
	int i, len = size;
	for ( i = 0; i < len; ++i ){
		dst[i] = text[rand() % (sizeof text - 1)];
	}
	dst[i] = '\0';
	return dst;
}

void startETAService(){
	system("nohup ETAStart &");
	//system("touch /Users/boyda/ETA/.running");
	sleep(5);
}