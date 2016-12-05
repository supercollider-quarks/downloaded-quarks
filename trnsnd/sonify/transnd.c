/*

 This code is obsolete now!
 interpret every byte as float
 http://yota.tehis.net/
 copyright yota morimoto 2012, all rights reserved.
 v0.5

*/

#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]){
		
	char num;
	float scale = 1.0 / 128.f;
	
	FILE *pi, *po;
	
	if ((po = fopen("data.ts", "w")) == NULL){
		printf("ERROR: cannot open output file\n");
	} else if ((pi = fopen(argv[1], "r")) == NULL){
		printf("ERROR: cannot open input file\n");
	} else {
		fprintf(po, "[");
		while (!feof(pi)){
			fscanf(pi, "%c", &num);
			fprintf(po, "%f,", num * scale);
		}
		fprintf(po, "0]");
	}
	fclose(pi);
	fclose(po);
	return 0;
}