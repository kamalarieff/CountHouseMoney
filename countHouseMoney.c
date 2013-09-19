#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <dirent.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

typedef struct {
	float moneyUsed,moneyPay;
	char* name;
}Person ;
	
	
float calculate( Person housemate , float total , float perPerson){

	
	if ( perPerson > housemate.moneyUsed ){
			housemate.moneyPay = perPerson - housemate.moneyUsed ;
			return housemate.moneyPay;
	}else {
			housemate.moneyPay = housemate.moneyUsed - perPerson ;
			return housemate.moneyPay;
	}
	
}

int main(int argc, const char *argv[])
{
	Person housemates[6];

	int i;
	float total=0;

	for (i = 0; i < 6; i++) {
		char *test;
		test = (char*)malloc(50);

		printf("Name: ");
		scanf("%s",test);

		housemates[i].name = test;

		printf("Money used: ");
		scanf("%f",&housemates[i].moneyUsed);

		total += housemates[i].moneyUsed;
		
	}
	
	float numPeople = 6;
	float perPerson = total / numPeople;
	
	printf("\n############################################################\n");
	
	for (i = 0; i < 6; i++) {
		printf("Name: %s \t Money: %.2f\n",housemates[i].name,housemates[i].moneyUsed);
	}

	printf("############################################################\n\n");

	
	float temp;

	for (i = 0; i < 6; i++) {
		temp = calculate(housemates[i], total, perPerson);
		if ( housemates[i].moneyUsed > perPerson )
				printf("Name: %s \t Money must be paid: %.2f\n",housemates[i].name,temp);
		else {
				printf("Name: %s \t Must pay: %.2f\n",housemates[i].name,temp);
		}
	}
	
	return 0;
}
