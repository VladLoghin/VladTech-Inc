import { api } from "../http";

export async function pinProject(projectIdentifier, token) {
  const res = await api.post(
    `/projects/${projectIdentifier}/pin`,
    {},
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data;
}

export async function unpinProject(projectIdentifier, token) {
  const res = await api.delete(
    `/projects/${projectIdentifier}/pin`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data;
}

export async function isProjectPinned(projectIdentifier, token) {
  const res = await api.get(
    `/projects/${projectIdentifier}/is-pinned`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data;
}
