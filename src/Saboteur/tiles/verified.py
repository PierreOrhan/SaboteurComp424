import pandas
import numpy as np
df=pandas.read_csv("descriptor.csv",sep=" ")
cardIdx=df.values[:,0]
cardPower=df.values[:,1]
cardNumber=np.array(df.values[:,2],dtype=np.uint16)
print("There is ",np.sum(cardNumber[:19])," paths cards")
print("There is ",np.sum(cardNumber[19:])," action cards")