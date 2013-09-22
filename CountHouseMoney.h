#include <iostream>
#include <string>

using namespace std;

class Person{
	public:
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

void checkPay ( Person housemate, float total, float perPerson){
	if ( housemate.moneyUsed > perPerson )
			cout << "Name: " << housemate.name << "\tMoney must be paid: " << calculate(housemate,total,perPerson) << endl;
	else {
			cout << "Name: " << housemate.name << "\tMust pay: " << calculate(housemate,total,perPerson) << endl;
	}
}

