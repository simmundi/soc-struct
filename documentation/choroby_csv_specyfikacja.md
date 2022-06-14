# Specyfikacja pliku choroby.csv

W pliku zapisywane są informacje o każdym zdarzeniu immunizacyjnym (ZI) w epidemii obliczonej przez symulator pdyn. Możliwe ZI to zakażenie lub zaszczepienie dowolnego agenta.

Format pliku jest tekstowy. Zawartością jest prostokątna tablica liczb całkowitych, oddzielonych przecinkami. Każdy wiersz dotyczy pojedynczego ZI. Separator linii `\n`. W kolumnach zawarte są kategorie, o nazwach podanych w jednowierszowym nagłówku. Kolejność kolumn może być dowolna. Wiersze są posortowane, patrz niżej.

Kategorie, w dotychczasowej kolejności zapisu:

* **id** – numer agenta, którego dotyczy ZI. Numer odpowiada pozycji agenta w pliku wejściowym `agenci.dat`. Obecni są tu tylko ci agenci, którzy w ciągu symulacji mieli jakieś ZI. Każdy agent może mieć zapisanych kilka ZI. Plik jest posortowany wg tej kolumny.
* **dzien_zakazenia** – numer dnia symulacji (liczony od dnia 0), w którym nastąpiło dane ZI. W przypadku zakażenia oznacza to pierwszy dzień stanu latent. W przypadku szczepienia – dzień szczepienia. Ujemne liczby oznaczają zakażenia z zasiewu początkowego, który odbywa się w ciągu kilku dni przed rozpoczęciem symulacji, aby w momencie rozpoczęcia zapewnić odpowiednią liczbę agentów na różnym etapie rozwoju choroby. Wpisy dla poszczególnych agentów są posortowane wg tej kolumny.
* ***dzien_wyzdrowienia*** – numer dnia symulacji (liczony od 0), w którym nastąpiło wyzdrowienie agenta. W przypadku szczepienia wartość -1. 
* ***objawy*** – wartość 1 dla zakażeń objawowych, 0 dla bezobjawowych, -1 dla szczepień. 
* ***zrodlo*** – źródło zakażenia, czyli w przypadku zakażeń numer agenta, który wywołał dane ZI. W przypadku zakażenia na ulicy wartość -1 (nieokreślone źródło). W przypadku szczepień również wartość -1. 
* ***zarazil*** – w założeniu liczba agentów zarażona przez agenta, którego dotyczy dane ZI. Obecnie zawiera wartości 0 lub 4294967295, wyglądające jak przekroczenie zakresu. 
* **miejsce_zakazenia** – kontekst, w którym nastąpiło zakażenie, wg konwencji: 0=dom, 1=przedszkole, 2=szkoła, 3=JEW, 4=DJEW, 5=praca, 6=podróż, 7=ulica. W przypadku szczepienia jest to również 0 (chociaż pasowałoby raczej -1).
* ***izolacja*** – w założeniu fakt poddania agenta izolacji po zakażeniu. Obecnie wszystkie wpisy mają wartość 0. 
* **odmiana_wirusa** – numer odmiany wirusa w przypadku zakażenia: 0=wild type, 1=alpha, 2=delta, 3=omikron, kolejne numery możliwe dla kolejnych przyszłych odmian. Dla szczepień wartość -1.
* **odmiana_szczepionki** – numery odmian szczepionki, obecnie: 0=szczepienie „podstawowe”, 1=booster, wyższe numery możliwe dla kolejnych przyszłych odmian. Dla zakażeń wartość -1.
* **historia_stanow** – zakodowany przebieg choroby, suma wartości odpowiadających stanom występującym w danym zakażeniu: latent=2, bezobj=4, obj=8, wyzdrow=16, hosp=32, preoiom=64, oiom=128, zmarly=256. Dla szczepień wartość 0. W uwagach poniżej przykładowy algorytm do konwersji.


## Uwagi:

1. Obecnie kilka kolumn (oznaczonych pochyłą czcionką na powyższej liście) nie jest wykorzystywanych w analizach w generatorze raportów (RG): 


	* ***dzien_wyzdrowienia*** – ta informacja zawarta jest w pozostałych danych (dzien_zakazenia i historia stanów, a także czasy trwania stanów, które bywały modyfikowane na etapie analizy w RG); można usunąć.
	* ***objawy*** – ta informacja zawarta jest też w historii_stanow; można usunąć.
	* ***zrodlo*** – w RG dotychczas nie używana, ale zawiera istotną informację do śledzenia kontaktów.
	* ***zarazil*** – dotychczas nie była wykorzystywana. Obecnie zawiera błędne dane; można usunąć. Teoretycznie ta informacja jest też do wyciągnięcia z kolumny zrodlo. 
	* ***izolacja*** – dotychczas nie była w ogóle analizowana, a w obecnej wersji zawiera same 0; można usunąć.

2. Pozostałe kolumny są używane w RG:

	* **id** – potrzebna do wszystkich analiz przestrzennych (powiązanie agenta z gospodarstwem domowym i lokalizacją), wiekowych, oraz do reinfekcji i infekcji po szczepieniu.
	* **dzien_zakazenia** – potrzebna do wszystkich analiz.
	* **miejsce_zakazenia** – potrzebna tylko do analizy zakażeń w kontekstach.
	* **odmiana_wirusa** – potrzebna do wszystkich analiz.
	* **odmiana_szczepionki** – potrzebna do wszystkich analiz.
	* **historia_stanow** – potrzebna do wszystkich analiz.


3. Fragment kodu RG (python3) do dekodowania kolumny **historia_stanow** 

		stany=["podatny","latent","bezobj","obj","wyzdrow","hosp","preoiom","oiom","zmarly"]
		choroby["hs2"]=choroby["historia_stanow"]//2
		for s in range(1,len(stany)):
			choroby[stany[s]]=choroby["hs2"]%2
			choroby[stany[s]]=choroby[stany[s]].astype(bool)
			choroby["hs2"]=choroby["hs2"]//2
		del choroby["hs2"] 

	W wyniku działania kodu tworzone są w ramce pandas nowe kolumny o nazwach wg zdefiniowanej tablicy `stany`, zawierające `True` w przypadkach, gdy dany stan występował w historii choroby agenta.
