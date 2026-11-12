"""Convierte un .bbmodel (formato GeckoLib, v5) a geo.json + animation.json de GeckoLib.
Sigue las mismas reglas que el exportador Bedrock de Blockbench (X invertida, rotaciones X/Y invertidas)."""
import json, sys

src, geo_out, anim_out, ident = sys.argv[1:5]
m = json.load(open(src, encoding='utf-8'))
res = m.get('resolution', {'width': 16, 'height': 16})
groups = {g['uuid']: g for g in m['groups']}
elements = {e['uuid']: e for e in m['elements']}

def num(v):
    v = float(v)
    return int(v) if v.is_integer() else round(v, 4)

def compile_cube(c):
    frm, to = c['from'], c['to']
    size = [num(to[i] - frm[i]) for i in range(3)]
    origin = [num(-(frm[0] + size[0])), num(frm[1]), num(frm[2])]
    cube = {'origin': origin, 'size': size}
    if c.get('inflate'):
        cube['inflate'] = c['inflate']
    rot = c.get('rotation', [0, 0, 0])
    if any(r != 0 for r in rot):
        piv = c['origin']
        cube['pivot'] = [num(-piv[0]), num(piv[1]), num(piv[2])]
        cube['rotation'] = [num(-rot[0]), num(-rot[1]), num(rot[2])]
    uv = {}
    for key, f in c['faces'].items():
        if f.get('texture') is None:
            continue
        u0, v0, u1, v1 = f['uv']
        e = {'uv': [num(u0), num(v0)], 'uv_size': [num(u1 - u0), num(v1 - v0)]}
        if f.get('rotation'):
            e['uv_rotation'] = f['rotation']
        if key in ('up', 'down'):
            e['uv'] = [num(e['uv'][0] + e['uv_size'][0]), num(e['uv'][1] + e['uv_size'][1])]
            e['uv_size'] = [num(-e['uv_size'][0]), num(-e['uv_size'][1])]
        uv[key] = e
    cube['uv'] = uv
    return cube

bones = []
def walk(nodes, parent=None):
    for n in nodes:
        if not isinstance(n, dict):
            continue
        g = groups[n['uuid']]
        bone = {'name': g['name']}
        if parent:
            bone['parent'] = parent
        o = g['origin']
        bone['pivot'] = [num(-o[0]), num(o[1]), num(o[2])]
        r = g.get('rotation', [0, 0, 0])
        if any(x != 0 for x in r):
            bone['rotation'] = [num(-r[0]), num(-r[1]), num(r[2])]
        cubes = [compile_cube(elements[c]) for c in n['children']
                 if isinstance(c, str) and c in elements and elements[c].get('export', True)]
        if cubes:
            bone['cubes'] = cubes
        bones.append(bone)
        walk(n['children'], g['name'])
walk(m['outliner'])

geo = {
    'format_version': '1.12.0',
    'minecraft:geometry': [{
        'description': {
            'identifier': 'geometry.' + ident,
            'texture_width': res['width'],
            'texture_height': res['height'],
            'visible_bounds_width': 3,
            'visible_bounds_height': 3.5,
            'visible_bounds_offset': [0, 1.25, 0],
        },
        'bones': bones,
    }]
}
json.dump(geo, open(geo_out, 'w', encoding='utf-8'), indent=2)

anims = {}
for a in m.get('animations', []):
    out = {}
    if a.get('loop') == 'loop':
        out['loop'] = True
    elif a.get('loop') == 'hold':
        out['loop'] = 'hold_on_last_frame'
    out['animation_length'] = a['length']
    ab = {}
    for an in a.get('animators', {}).values():
        if an.get('type', 'bone') != 'bone':
            continue
        chans = {}
        for kf in an.get('keyframes', []):
            ch = kf['channel']
            if ch not in ('rotation', 'position', 'scale'):
                continue
            dp = kf['data_points'][0]
            vals = [num(dp['x']), num(dp['y']), num(dp['z'])]
            if ch == 'position':
                vals[0] = num(-vals[0])
            if ch == 'rotation':
                vals[0] = num(-vals[0]); vals[1] = num(-vals[1])
            t = str(num(kf['time']))
            if '.' not in t:
                t += '.0'
            if kf.get('interpolation') == 'catmullrom':
                chans.setdefault(ch, {})[t] = {'post': vals, 'lerp_mode': 'catmullrom'}
            else:
                chans.setdefault(ch, {})[t] = vals
        for ch in chans:
            chans[ch] = dict(sorted(chans[ch].items(), key=lambda kv: float(kv[0])))
        if chans:
            ab[an['name']] = chans
    out['bones'] = ab
    anims[a['name']] = out
json.dump({'format_version': '1.8.0', 'animations': anims, 'geckolib_format_version': 2},
          open(anim_out, 'w', encoding='utf-8'), indent=2)
print('bones', len(bones), 'cubes', sum(len(b.get('cubes', [])) for b in bones), 'anims', list(anims))
