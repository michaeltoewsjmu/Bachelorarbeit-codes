import arcpy
import os

output_csv = r"C:\Users\micha\Desktop\ET_Vollständige_Statistik.csv"

p = arcpy.mp.ArcGISProject("CURRENT")
m = p.listMaps()[0]

print(f"Starte erweiterten Export (Mean, Min, Max, Std, Median)...")
print(f"Zieldatei: {output_csv}")

with open(output_csv, "w", encoding='utf-8-sig') as f:
    
    f.write("Dateiname;Jahr;Monat;Sensor;Mean;Min;Max;StdDev;Median;PixelCount\n")

    count = 0
    
    for lyr in m.listLayers():
        if lyr.isRasterLayer:
            try:

                def get_stat(prop):
                    val = arcpy.GetRasterProperties_management(lyr, prop).getOutput(0)
                    return val.replace('.', ',')

                mean_val = get_stat("MEAN")
                min_val = get_stat("MINIMUM")
                max_val = get_stat("MAXIMUM")
                std_val = get_stat("STD")
                
                desc = arcpy.Describe(lyr)

                median_val = "n/a" 
                pixel_count = "n/a" 

                name = lyr.name
                jahr = ""
                monat = ""
                sensor = ""

                for y in range(2000, 2026):
                    if str(y) in name:
                        jahr = str(y); break
                
                if "GLEAM" in name.upper(): sensor = "GLEAM"
                elif "MOD" in name.upper() or "MODIS" in name.upper(): sensor = "MODIS"
                else: sensor = "Unbekannt"

                for m_num in range(1, 13):
                    if f"_{m_num:02d}" in name:
                        monat = f"{m_num:02d}"; break
                if monat == "": monat = "Jahr"

                
                line = f"{name};{jahr};{monat};{sensor};{mean_val};{min_val};{max_val};{std_val};{median_val};{pixel_count}\n"
                f.write(line)
                
                count += 1
                if count % 20 == 0: print(f"{count} Layer...")

            except Exception as e:
                pass

print(f"\nFERTIG! {count} Datensätze exportiert (Mean, Min, Max, Std).")
