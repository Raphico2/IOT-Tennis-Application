import math
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
import numpy as np
from scipy.signal import find_peaks
from pickle import load

def replace_outliers_with_mean(column):
    # Replace the outliers by the column mean
    # outlier define are the  0.99 quantile of the column
    upper = column.quantile(0.99)
    outliers = column[(column > upper)]
    column[outliers.index] = column.mean()

    return column


def normalize(columns_to_normalize, dataframe):
    # Apply Min max normalisation on the column to normalize
    scaler = MinMaxScaler()
    dataframe[columns_to_normalize] = scaler.fit_transform(dataframe[columns_to_normalize])
    return dataframe

def distance_euclidienne(liste1, liste2):
    if len(liste1) != len(liste2):
        raise ValueError("Les listes doivent avoir la même longueur")

    distance = math.sqrt(sum([(x - y) ** 2 for x, y in zip(liste1, liste2)]))
    return distance

def assign_profile_tennis(list_value):
    # Consistency, regularity, technicity, mean speed cd, mean spped rv

    profil_Federer = [75, 80, 100, 100, 78]
    profil_Nadal = [90, 95, 65, 98, 90]
    profil_Djokovic = [100, 100, 100, 70, 70]
    profil_Murray = [80, 75, 80, 67, 60]
    profil_Wawrinka = [50, 45, 80, 90, 100]
    profil_beginer = [10, 10, 2, 3, 7]
    profil_list = [profil_Federer, profil_Nadal, profil_Djokovic, profil_Murray, profil_Wawrinka, profil_beginer]
    min_distance=100000
    index = 0
    for i,profil in enumerate(profil_list):
        distance = distance_euclidienne(list_value, profil)
        if distance < min_distance:
            min_distance = distance
            index = i

    if index == 0:
        sentence_profil = "You have the profile of Roger Federer"
    elif index == 1:
        sentence_profil = "You have the profile of Rafael Nadal"
    elif index == 2:
        sentence_profil = "You have the profile of Novak Djokovic"
    elif index == 3:
        sentence_profil = "You have the profile of Andy Murray"
    elif index == 4:
        sentence_profil = "You have the profile of Stan Wawrinka"
    else:
        sentence_profil = "You have the profile of a beginner"

    return sentence_profil

def assign_profile_run(list_value):
    # Consistency, regularity, technicity, mean speed cd, mean spped rv
    profil_UsainBolt = [100, 100]
    profil_turtle = [0, 0]
    profil_list = [profil_UsainBolt, profil_turtle]
    min_distance=100000
    index = 0
    for i,profil in enumerate(profil_list):
        distance = distance_euclidienne(list_value, profil)
        if distance < min_distance:
            min_distance = distance
            index = i

    if index == 0:
        sentence_profil = "You have the profile of Usain Bolt"
    else:
        sentence_profil = "You have the profile of a Turtle"
    return sentence_profil


def stat_tenis(coups, preds):
    # Calculate some statistic on all the shot like the number of shot, number of backhand, number of forhand
    # low-speed and high-speed, max-low-speed,max-high-speed etc
    peaks, _ = find_peaks(coups['ANorm'].values, distance=34/2, height=0.94)
    print("lenpozkcedl",len(peaks))
    nb_de_coup = len(peaks)
    diff = np.diff(peaks)
    echange = [peaks[i]  for i in range(len(diff)) if  diff[i]>34*1.5] #
    nb_echange = len(echange)+11
    nb_arret = len(echange)
    nb_coup_par_echange = []
    if len(echange) > 0:
        for i in range(len(echange)):
            if i == 0:
                pks, _ = find_peaks(coups['ANorm'][0:echange[i]], distance=34/2, height=0.94)
                nb_coup_par_echange.append(len(pks))
            else:
                pks, _ = find_peaks(coups['ANorm'][echange[i-1]:echange[i]], distance=34/2, height=0.94)
                nb_coup_par_echange.append(len(pks))
        print(echange)
        if len(peaks) > 6 and echange[-1]!=peaks[-2]:
            idx = list(peaks).index(echange[-1])
            pks, _ = find_peaks(coups['ANorm'][peaks[idx]:peaks[-1]], distance=34/2, height=0.94)
            nb_coup_par_echange.append(len(pks))
        if len(nb_coup_par_echange) == 0:
            pks, _ = find_peaks(coups['ANorm'], distance=34/2, height=0.94)
            nb_coup_par_echange.append(len(pks))
    else:
        pks, _ = find_peaks(coups['ANorm'], distance=34/2, height=0.94)
        nb_coup_par_echange.append(len(pks))

    moy_coup_par_echange = np.mean(nb_coup_par_echange)
    var_coup_par_echange = np.var(nb_coup_par_echange)
    max_coup_par_echange = max(nb_coup_par_echange)

    vitesse_basse = []
    vitesse_haute = []
    vitesse_par_echange = []
    v = [0,0]
    ech = 1
    if len(diff)==0:
        vitesse_basse.append(0.0)
        vitesse_haute.append(0.0)
    for i in range(len(diff)):

        if diff[i] > 34*1.5:
            vitesse_basse.append(3.6*21/(0.045*diff[i-1]))
            vitesse_haute.append(3.6*26/(0.045*diff[i-1]))
            v[0] = float(v[0]/ech)
            v[1] = float(v[1]/ech)
            vitesse_par_echange.append(v)
            v = [0,0]
            ech = 1
        else:
            vitesse_basse.append(3.6*21/(0.045*diff[i]))
            vitesse_haute.append(3.6*26/(0.045*diff[i]))

        v[0] += 3.6*21/(0.045*diff[i])
        v[1] += 3.6*26/(0.045*diff[i])
        ech +=1


    moyenne_vitesse_basse = np.mean(vitesse_basse)
    moyenne_vitesse_haute = np.mean(vitesse_haute) #qur tous le csv
    max_vitesse_basse = np.max(vitesse_basse)
    max_vitesse_haute = np.max(vitesse_haute)
    moy_vit_h_cd = []
    moy_vit_b_cd = []
    moy_vit_h_rv = []
    moy_vit_B_rv = []
    for i in range(len(vitesse_basse)):
        if preds[i]==0:
            moy_vit_h_cd.append(vitesse_haute[i])
            moy_vit_b_cd.append(vitesse_basse[i])
        else:
            moy_vit_h_rv.append(vitesse_haute[i])
            moy_vit_B_rv.append(vitesse_basse[i])

    nombre_de_cd = list(preds).count(0)
    nombre_de_rv = list(preds).count(1)
    consitency = 100-var_coup_par_echange
    technicity = 50 + np.min([(nombre_de_rv*100)/(nombre_de_rv+nombre_de_cd), (nombre_de_cd*100)/(nombre_de_rv+nombre_de_cd)])
    regularity = moy_coup_par_echange*10
    if len(moy_vit_h_cd)>0:
        mean_speed_cd = np.mean(moy_vit_h_cd)
    else:
        mean_speed_cd = 0
    if len(moy_vit_h_rv)>0:
        mean_speed_rv = np.mean(moy_vit_h_rv)
    else:
        mean_speed_rv = 0
    print(moy_vit_h_cd)
    if mean_speed_cd==None:
        mean_speed_cd = 20
    sentence_profile = assign_profile_tennis([consitency, technicity, regularity, mean_speed_cd, mean_speed_rv])
    to_return = str(nb_de_coup)+';'+str(nombre_de_rv)+';'+str(nombre_de_cd)+';'+str(int(moyenne_vitesse_basse))+'-'+str(int(moyenne_vitesse_haute))+'Km/h'+';'\
                +str(int(max_vitesse_basse))+'-'+str(int(max_vitesse_haute))+'km/h' +';'+str(max_coup_par_echange)+';'+str(moy_coup_par_echange) \
                +';'+str(consitency)+';'+str(technicity)+";"+str(regularity)+";"+str(mean_speed_cd) \
                +";" + str(mean_speed_rv)+";" + sentence_profile
    print(to_return)

    return to_return


def create_df(path):
    # Create and preprocess the data
    cd_df = pd.read_csv(path, skiprows=1)
    cd_df.reset_index(drop=True, inplace=True)
    cd_df = cd_df.apply(pd.to_numeric, errors='coerce')
    replace_outliers_with_mean

    for col in list(cd_df.columns):
        cd_df[col]  = replace_outliers_with_mean(cd_df[col])

    cd_df = normalize(list(cd_df.columns),cd_df)
    #cd_df = cd_df.fillna(cd_df.mean())
    cd_df = cd_df.interpolate()

    return cd_df



def make_even(df):
    # check if the walk_or_run file is longer than avg_len and if yes create several dataset of size avg_len
    # if the longer of the last dataset created is longer than shorter than 0.72*avg_len i return the different file
    avg_len=695
    data_list = []
    if(df.shape[0]==avg_len):
        return data_list.append(df)
    if df.shape[0] > avg_len:
        start = 0
        end = avg_len
        while end <= df.shape[0]:
            data_list.append(df[start:end])
            start = end
            end += avg_len
        remaining = df[start:]
        if remaining.shape[0] < 0.72 * avg_len:
            return data_list
        else:
            df = remaining

    extra_df = df.copy()
    while extra_df.shape[0] < avg_len:
        extra_df = pd.concat([extra_df, df])
    data_list.append(extra_df.head(avg_len))
    return data_list


def check_run(str):
    if (str=='Running'):
        return 1
    else:
        return 0

def predict_coupdroit(df):
    # Predict for each shot if it is a  backhand or forhand

    model = load(open('storage/emulated/0/Download/model_tenis.pkl', 'rb'))

    cps = df
    peaks, _ = find_peaks(cps['ANorm'].values, distance=34/2, height=0.94)
    middle = []
    l=[]
    data = []
    print(peaks)
    for j in range(0, len(peaks)):
        if peaks[j]-6<1:
            continue
        l= list(cps['Mx'][peaks[j]-6:peaks[j]].values)
        l.extend(list(cps['My'][peaks[j]-6:peaks[j]].values))
        l.extend(list(cps['Mz'][peaks[j]-6:peaks[j]].values))
        l.extend(list(cps['Gx'][peaks[j]-6:peaks[j]].values))
        l.extend(list(cps['Gy'][peaks[j]-6:peaks[j]].values))
        l.extend(list(cps['Gz'][peaks[j]-6:peaks[j]].values))
        data.append(l)
    column_names = [i for i in range(36)]

    array = np.stack(data)
    df = pd.DataFrame(array,columns=column_names)
    df =df.dropna()
    preds = model.predict(df)
    return preds


def predict_running(df):
    # Predict if we are Running or Walking
    extra_array=pd.DataFrame()
    model = load(open('storage/emulated/0/Download/model_running.pkl', 'rb'))


    data = df
    #data = pd.read_csv(path_csv, skiprows = 1)

    data['peak']= data['ANorm']
    list_data=make_even(data)
    for i in range(len(list_data)):
        extra_array=pd.concat([extra_array,pd.Series(list_data[i].T.loc['peak'].to_list())], axis=1)
    extra_array=extra_array.T.reset_index().drop(['index'],axis=1)
    X_test=extra_array
    yhat = model.predict(X_test)
    preds=list(map(str, yhat) )
    for indexa, yi in enumerate(yhat):
        if yi ==1:
            preds[indexa] = 'Running'
        else:
            preds[indexa] = 'Walking'
    return preds[0]

def main():
    path_csv = "/storage/emulated/0/Download/memory.csv"
    first_row = pd.read_csv(path_csv, nrows=1)
    df = create_df(path_csv)
    length = len(df['ANorm'])
    if list(first_row.columns)[0] == 'run':
        prediction = predict_running(df)
        distance = 0
        nombredepas= 0
        vit = 0

        if prediction == 'Walking':

            peaks, _ = find_peaks(df['ANorm'].values, distance=3.5, height=0.7)
            nombre_de_pas = len(peaks)
            distance = nombre_de_pas *0.8

            vitesse = (3.6*distance)/(0.045*length)
        else:
            peaks, _ = find_peaks(df['ANorm'].values, distance=1, height=0.7)
            nombre_de_pas = len(peaks)
            distance = nombre_de_pas *1.1

        sentence_profile = assign_profile_run([vitesse, distance])
        to_return = str(prediction)+';'+str(int(vitesse))+ ';'+str(nombre_de_pas)+';'+str(int(distance))+";"+str(sentence_profile)
        print(to_return)

        return to_return
    else:
        preds = predict_coupdroit(df)
        return stat_tenis(df,preds)

if __name__ == "__main__":
    main()
