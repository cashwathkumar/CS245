#include "Traffic.h"
#include "sys/time.h"
#include "signal.h"
#include "stdlib.h"

volatile unsigned char TimerFlag = 0;

void TimerHandler() {
	TimerFlag = 1;
}

typedef struct {
	signed char state;
	unsigned long period;
	unsigned long elapsedTime;
	int (*TickFct)(int);
} task_t;

enum SIG1_states { SIG1_Green, SIG1_PreYellow, SIG1_Yellow, SIG1_PreRed, SIG1_Red, SIG1_PreGreen };
enum SIG2_states { SIG2_Red, SIG2_PreYellow, SIG2_Yellow, SIG2_PreRed, SIG2_Green, SIG2_PreGreen };
enum SIG3_states { SIG3_Red, SIG3_PreYellow, SIG3_Yellow, SIG3_PreRed, SIG3_Green, SIG3_PreGreen };
enum SIG4_states { SIG4_Red, SIG4_PreYellow, SIG4_Yellow, SIG4_PreRed, SIG4_Green, SIG4_PreGreen };

int SIG1_Tick(int state) {
	switch(state) { // Transitions
		case -1:
			state = SIG1_Green;
			break;
		case SIG1_Green:
			if(Count1 > 20)
			{
				state = SIG1_PreYellow;
			}
			break;
		case SIG1_PreYellow:
			if(1)
			{
				state = SIG1_Yellow;
			}
			break;
		case SIG1_Yellow:
			if(Count1 > 3)
			{
				state = SIG1_PreRed;
			}
			break;
		case SIG1_PreRed:
			if(1)
			{
				state = SIG1_Red;
			}
			break;
		case SIG1_Red:
			if(Sig1 == 0)
			{
				state = SIG1_PreGreen;
			}
			break;
		case SIG1_PreGreen:
			if(1)
			{
				state = SIG1_PreYellow;
			}
			break;
		default:
			state = -1;
			break;
	}

	switch(state) { // State actions
		case SIG1_Green:
			Out1 = 2;
			Count1++;
			break;
		case SIG1_PreYellow:
			Out1 = 1;
			Count1 = 0;
			break;
		case SIG1_Yellow:
			Out1 = 1;
			Count1++;
			break;
		case SIG1_PreRed:
			Out1 = 0;
			Sig2 = 1;
			break;
		case SIG1_Red:
			Out1 = 0;
			break;
		case SIG1_PreGreen:
			Out1 = 2;
			Sig1 = 0;
			Count1 = 0;
			break;
		default:
			break;
	}
}

int SIG2_Tick(int state) {
	switch(state) { // Transitions
		case -1:
			state = SIG2_Red;
			break;
		case SIG2_Red:
			if(Sig2 == 0)
			{
				state = SIG2_PreGreen;
			}
			break;
		case SIG2_PreYellow:
			if(1)
			{
				state = SIG2_Yellow;
			}
			break;
		case SIG2_Yellow:
			if(Count2 > 3)
			{
				state = SIG2_PreRed;
			}
			break;
		case SIG2_PreRed:
			if(1)
			{
				state = SIG2_Red;
			}
			break;
		case SIG2_Green:
			if(Count2 > 20)
			{
				state = SIG2_PreYellow;
			}
			break;
		case SIG2_PreGreen:
			if(1)
			{
				state = SIG2_PreYellow;
			}
			break;
		default:
			state = -1;
			break;
	}

	switch(state) { // State actions
		case SIG2_Red:
			Out2 = 0;
			break;
		case SIG2_PreYellow:
			Out2 = 1;
			Count2 = 0;
			break;
		case SIG2_Yellow:
			Out2 = 1;
			Count2++;
			break;
		case SIG2_PreRed:
			Out2 = 0;
			Sig3 = 1;
			break;
		case SIG2_Green:
			Out2 = 2;
			Count2++;
			break;
		case SIG2_PreGreen:
			Out2 = 2;
			Sig2 = 0;
			Count2 = 0;
			break;
		default:
			break;
	}
}

int SIG3_Tick(int state) {
	switch(state) { // Transitions
		case -1:
			state = SIG3_Red;
			break;
		case SIG3_Red:
			if(Sig3 == 0)
			{
				state = SIG3_PreGreen;
			}
			break;
		case SIG3_PreYellow:
			if(1)
			{
				state = SIG3_Yellow;
			}
			break;
		case SIG3_Yellow:
			if(Count3 > 3)
			{
				state = SIG3_PreRed;
			}
			break;
		case SIG3_PreRed:
			if(1)
			{
				state = SIG3_Red;
			}
			break;
		case SIG3_Green:
			if(Count3 > 20)
			{
				state = SIG3_PreYellow;
			}
			break;
		case SIG3_PreGreen:
			if(1)
			{
				state = SIG3_PreYellow;
			}
			break;
		default:
			state = -1;
			break;
	}

	switch(state) { // State actions
		case SIG3_Red:
			Out3 = 0;
			break;
		case SIG3_PreYellow:
			Out3 = 1;
			Count3 = 0;
			break;
		case SIG3_Yellow:
			Out3 = 1;
			Count3++;
			break;
		case SIG3_PreRed:
			Out3 = 0;
			Sig4 = 1;
			break;
		case SIG3_Green:
			Out3 = 2;
			Count3++;
			break;
		case SIG3_PreGreen:
			Out3 = 2;
			Sig3 = 0;
			Count3 = 0;
			break;
		default:
			break;
	}
}

int SIG4_Tick(int state) {
	switch(state) { // Transitions
		case -1:
			state = SIG4_Red;
			break;
		case SIG4_Red:
			if(Sig4 == 0)
			{
				state = SIG4_PreGreen;
			}
			break;
		case SIG4_PreYellow:
			if(1)
			{
				state = SIG4_Yellow;
			}
			break;
		case SIG4_Yellow:
			if(Count4 > 3)
			{
				state = SIG4_PreRed;
			}
			break;
		case SIG4_PreRed:
			if(1)
			{
				state = SIG4_Red;
			}
			break;
		case SIG4_Green:
			if(Count4 > 20)
			{
				state = SIG4_PreYellow;
			}
			break;
		case SIG4_PreGreen:
			if(1)
			{
				state = SIG4_PreYellow;
			}
			break;
		default:
			state = -1;
			break;
	}

	switch(state) { // State actions
		case SIG4_Red:
			Out4 = 0;
			break;
		case SIG4_PreYellow:
			Out4 = 1;
			Count4 = 0;
			break;
		case SIG4_Yellow:
			Out4 = 1;
			Count4++;
			break;
		case SIG4_PreRed:
			Out4 = 0;
			Sig1 = 1;
			break;
		case SIG4_Green:
			Out4 = 2;
			Count4++;
			break;
		case SIG4_PreGreen:
			Out4 = 2;
			Sig4 = 0;
			Count4 = 0;
			break;
		default:
			break;
	}
}

void main()
{
	const unsigned long SIG1_period = 1000;
	const unsigned long SIG2_period = 1000;
	const unsigned long SIG3_period = 1000;
	const unsigned long SIG4_period = 1000;

	const unsigned long GCD = 1000;

	unsigned char i; // Index for scheduler's for loop

	struct itimerval itv;
	struct sigaction sa;

	static task_t task1, task2, task3, task4;
	task_t *tasks[] = { &task1, &task2, &task3, &task4 };
	const unsigned short numTasks = sizeof(tasks) / sizeof(task_t*);

	task1.state = -1;
	task1.period = SIG1_period;
	task1.elapsedTime = SIG1_period;
	task1.TickFct = &SIG1_Tick;

	task2.state = -1;
	task2.period = SIG2_period;
	task2.elapsedTime = SIG2_period;
	task2.TickFct = &SIG2_Tick;

	task3.state = -1;
	task3.period = SIG3_period;
	task3.elapsedTime = SIG3_period;
	task3.TickFct = &SIG3_Tick;

	task4.state = -1;
	task4.period = SIG4_period;
	task4.elapsedTime = SIG4_period;
	task4.TickFct = &SIG4_Tick;

	/* Initialize output variables*/
	Out1 = 0;
	Count1 = 0;
	Sig2 = 0;
	Sig1 = 0;
	Out2 = 0;
	Count2 = 0;
	Sig3 = 0;
	Out3 = 0;
	Count3 = 0;
	Sig4 = 0;
	Out4 = 0;
	Count4 = 0;

	/* Initialize signal*/
	sigemptyset(&sa.sa_mask);
	sa.sa_flags = 0;
	sa.sa_handler = TimerHandler;
	if (sigaction(SIGALRM, &sa, NULL) == -1)
	{
		exit(EXIT_FAILURE);
	}

	/* Initialize timer with the period*/
	itv.it_value.tv_sec = 1;
	itv.it_value.tv_usec = 0;
	itv.it_interval.tv_sec = 1;
	itv.it_interval.tv_usec = 0;

	/* Start timer*/
	if (setitimer(ITIMER_REAL, &itv, NULL) == -1)
	{
		exit(EXIT_FAILURE);
	}

	while(1) {
		for ( i = 0; i < numTasks; ++i ) {
			if ( tasks[i]->elapsedTime == tasks[i]->period ) {
				// Task is ready to tick, so call its tick function
				tasks[i]->state = tasks[i]->TickFct(tasks[i]->state);
				tasks[i]->elapsedTime = 0; // Reset the elapsed time
			}
			tasks[i]->elapsedTime += GCD; // Account for below wait
		}
		while(!TimerFlag); // Wait for next timer tick
		TimerFlag = 0;
	}
}
