#include <iostream>
#include <string>

using namespace std;

class Person{
	public:
	// float calculate( Person , float  , float );
	// float getMoneyUsed();
	// float getMoneyPay();
	// void setMoneyUsed(float);
	// void setMoneyPay(float);
	float moneyUsed,moneyPay;
	string name;
};

float calcPerPerson(float total, int numPeople ){
	return total / numPeople;
}

float calculate( Person housemate, float total, float perPerson){
	if ( perPerson > housemate.moneyUsed ){
			housemate.moneyPay = perPerson - housemate.moneyUsed ;
			return housemate.moneyPay;
	}else {
			housemate.moneyPay = housemate.moneyUsed - perPerson ;
			return housemate.moneyPay;
	}
}
	
// void Person::setMoneyUsed(float a){
// 	moneyUsed = a;
// }
// 
// void Person::setMoneyPay(float a){
// 	moneyPay = a;
// }
// 
// float Person::getMoneyUsed(){
// 	return moneyUsed;
// }
// 
// float Person::getMoneyPay(){
// 	return moneyPay;
// }



int main(int argc, const char *argv[])
{
	int numPeople = 6;
	Person housemates[numPeople];

	float total=0;

	int i;
	for (i = 0; i < numPeople; i++) {
		cout << "Enter name: " ;
		cin >> housemates[i].name ;
		cout << "Enter money used: " ;
		cin >> housemates[i].moneyUsed;
		
		total += housemates[i].moneyUsed;
	}
	
	float perPerson = calcPerPerson(total,numPeople);

	for (i = 0; i < numPeople; i++) {
			float temp = calculate(housemates[i], total, perPerson);

			if ( housemates[i].moneyUsed > perPerson )
					// printf("Name: %s \t Money must be paid: %.2f\n",housemates[i].name,temp);
					cout << "Name: " << housemates[i].name << "\tMoney must be paid: " << temp << endl;
			else {
					// printf("Name: %s \t Must pay: %.2f\n",housemates[i].name,temp);
					cout << "Name: " << housemates[i].name << "\tMust pay: " << temp << endl;
			}
	}
	
	return 0;
}
