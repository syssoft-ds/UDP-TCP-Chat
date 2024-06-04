#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <ctype.h>
#include <unistd.h>

#include <sys/socket.h>
#include <arpa/inet.h>

#define BUFFER_SIZE 1024

void fatal ( char *comment ) {
  fprintf(stderr,"Error: %s\n",comment);
  perror("perror() returns:");
  exit(-1);
}

void toLowerCase ( char *s ) {
  for (int i=0; s[i]; i++) s[i] = tolower(s[i]);
}

struct sockaddr *address_of ( char *host, int port ) {
  struct sockaddr_in *addr = (struct sockaddr_in *) malloc(sizeof(struct sockaddr_in));
  memset((char *) addr,0,sizeof(struct sockaddr_in));
  addr->sin_family = AF_INET;
  addr->sin_port = htons(port);
  if (host == NULL)
    addr->sin_addr.s_addr = htonl(INADDR_ANY);
  else {
    int ret = inet_aton(host,&(addr->sin_addr));
    if (ret == 0) fatal("inet_aton() failed to convert IP address of peer");
  }
  return (struct sockaddr *) addr;
}

void receiveLines ( int port ) {
  int s = socket(AF_INET,SOCK_DGRAM,0);
  if (s == -1) fatal("Unable to create socket");
  struct sockaddr *my_addr = address_of(NULL,port);
  int ret = bind(s ,my_addr, sizeof(struct sockaddr_in));
  free((char *) my_addr);
  if (ret == -1) fatal("Unable to bind socket to given port");
  char buffer[BUFFER_SIZE];
  while (1) {
    int message_length = recvfrom(s,buffer,BUFFER_SIZE-1,0,NULL,NULL);
    buffer[message_length] = 0;
    if (message_length == -1) fatal("recvfrom() failed");
    printf("%s\n",buffer);
    toLowerCase(buffer);
    if (strcmp(buffer,"stop") == 0) break;
  }
  close(s);
}

void sendLines ( char *host, int port ) {
  int s = socket(AF_INET,SOCK_DGRAM,0);
  if (s == -1) fatal("Unable to create socket");
  struct sockaddr *peer = address_of(host,port);
  char *buffer = malloc(BUFFER_SIZE);
  while (1) {
    size_t bsize = BUFFER_SIZE;
    int ret = getline(&buffer,&bsize,stdin);
    if (ret == -1) fatal("getline() from stdin failed");
    ret = sendto(s,buffer,strlen(buffer),0,peer,sizeof(struct sockaddr_in));
    if (ret == -1) fatal("sendto() failed");
    toLowerCase(buffer);
    if (strcmp(buffer,"stop") == 0) break;
  }
  close(s);
  free(buffer);
  free((char *) peer);
}

int main ( int ac, char **av ) {
  if (ac != 3) {
    fprintf(stderr,"Usage: \"%s -l <port>\" or \"%s <host> <port>\"\n",av[0],av[0]);
    exit(-1);
  }

  int port = strtol(av[2],NULL,10);
  toLowerCase(av[1]);
  if (strncmp(av[1],"-l",2) == 0)
    receiveLines(port);
  else
    sendLines(av[1],port);
}
